package io.jexxa.common.drivenadapter.outbox;

import io.jexxa.adapterapi.JexxaContext;
import io.jexxa.adapterapi.invocation.InvocationManager;
import io.jexxa.adapterapi.invocation.InvocationTargetRuntimeException;
import io.jexxa.common.drivenadapter.messaging.MessageBuilder;
import io.jexxa.common.drivenadapter.messaging.MessageSender;
import io.jexxa.common.drivenadapter.messaging.jms.JMSSender;
import io.jexxa.common.drivenadapter.persistence.repository.IRepository;
import io.jexxa.common.drivenadapter.persistence.repository.imdb.IMDBRepository;
import io.jexxa.common.drivenadapter.persistence.repository.jdbc.JDBCKeyValueRepository;
import io.jexxa.common.facade.logger.SLF4jLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.jexxa.common.drivenadapter.messaging.MessageSenderFactory.createMessageSender;
import static io.jexxa.common.drivenadapter.messaging.MessageSenderFactory.setMessageSender;
import static io.jexxa.common.drivenadapter.outbox.TransactionalOutboxProperties.outboxTable;
import static io.jexxa.common.drivenadapter.persistence.RepositoryFactory.createRepository;
import static io.jexxa.common.facade.logger.SLF4jLogger.getLogger;


/**
 * This class implements the  <a href="https://microservices.io/patterns/data/transactional-outbox.html">transactional outbox pattern</a>.
 * This class encapsulates both parts, storing messages to a database within the transaction of the incoming method call
 * and the message relay part.
 * <br>
 * In the current implementation, we check each 300 ms if a new message is available that is then forwarded.
 */
public class TransactionalOutboxSender extends MessageSender {
    private static final List<TransactionalOutboxSender> TRANSACTIONAL_OUTBOX_SENDERS = new ArrayList<>();
    private static boolean cleanupRegistered = false;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final IRepository<JexxaOutboxMessage, UUID> outboxRepository;
    private final MessageSender messageSender;


    @SuppressWarnings("unused") // used by MessageSenderFactory
    public static MessageSender createInstance(Properties properties)
    {
        var transactionalOutboxSender = new TransactionalOutboxSender(properties);
        TRANSACTIONAL_OUTBOX_SENDERS.add(transactionalOutboxSender);

        if (!cleanupRegistered)
        {
            JexxaContext.registerCleanupHandler(TransactionalOutboxSender::cleanup);
            cleanupRegistered = true;
        }
        return transactionalOutboxSender;
    }

    private TransactionalOutboxSender(Properties properties)
    {
        this.outboxRepository = createRepository(JexxaOutboxMessage.class
                        , JexxaOutboxMessage::messageId
                        , properties );

        if (this.outboxRepository instanceof IMDBRepository<JexxaOutboxMessage, UUID>) {
            SLF4jLogger.getLogger(TransactionalOutboxSender.class).warn("Your TransactionalOutboxSender uses an IMDBRepository for persisting unsent messages. This might be fine for testing purposes. In production environment define a JDBC connection for proper message resend.");
        }

        if (this.outboxRepository instanceof JDBCKeyValueRepository<JexxaOutboxMessage, UUID>) {
            if (properties.containsKey(outboxTable()) && !properties.getProperty(outboxTable()).isEmpty())
            {
                ((JDBCKeyValueRepository<JexxaOutboxMessage, UUID >)(this.outboxRepository))
                        .tableName(properties.getProperty(outboxTable()));
            } else {
                if (!TRANSACTIONAL_OUTBOX_SENDERS.isEmpty()) {
                    SLF4jLogger.getLogger(TransactionalOutboxSender.class).warn("You use multiple instances of TransactionalOutboxSender -> Define a dedicated table for each connection using `unique-prefix`.outbox.table .");
                }
            }
        }

        setMessageSender(JMSSender.class, TransactionalOutboxSender.class); // Ensure that we get a JMSSender for internal sending
        this.messageSender = createMessageSender(TransactionalOutboxSender.class, properties);

        executor.schedule( this::transactionalSend, 300, TimeUnit.MILLISECONDS);
    }

    public static void cleanup()
    {
        TRANSACTIONAL_OUTBOX_SENDERS.forEach(TransactionalOutboxSender::internalCleanup);
        TRANSACTIONAL_OUTBOX_SENDERS.clear();
    }

    void internalCleanup() {
        try {
            executor.shutdown();
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                getLogger(this.getClass()).warn("Could not successfully stop running operations -> Force shutdown");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            getLogger(TransactionalOutboxSender.class).warn("ExecutorService could not be stopped -> Interrupt thread.", e);
            Thread.currentThread().interrupt();
        }

        if (messageSender instanceof AutoCloseable autoCloseable)
        {
            try {
                autoCloseable.close();
            } catch (Exception e)
            {
                getLogger(TransactionalOutboxSender.class).error(e.getMessage());
            }
        }
    }

    /**
     * This method is the entry point for the message relay part of the transactional outbox pattern.
     * It calls method {@link #sendOutboxMessages()} in a transaction managed by the invocation manager
     */
    @SuppressWarnings("java:S1181")
    public void transactionalSend()
    {
        try {
            var handler = InvocationManager.getInvocationHandler(this);
            handler.invoke(this, this::sendOutboxMessages);
        } catch (InvocationTargetRuntimeException e)
        {
            getLogger(getClass()).warn("Could not send outbox messages. Reason: {}", e.getTargetException().getMessage());
            getLogger(getClass()).debug("Stack Trace", e);
        } catch (Throwable e)
        {
            getLogger(getClass()).error("{} occurred in transactionalSend. Reason: {}", e.getClass().getSimpleName(), e.getMessage());
            getLogger(getClass()).debug("Stack Trace", e);
        }
    }

    @Override
    protected synchronized void sendToQueue(String message, String destination, Properties messageProperties, MessageType messageType) {
        outboxRepository.add(new JexxaOutboxMessage(
                UUID.randomUUID(), message,
                destination, messageProperties,
                messageType, DestinationType.QUEUE));
        executor.schedule( this::transactionalSend,0, TimeUnit.MICROSECONDS);
    }

    @Override
    protected synchronized void sendToTopic(String message, String destination, Properties messageProperties, MessageType messageType) {
        outboxRepository.add(new JexxaOutboxMessage(
                UUID.randomUUID(), message,
                destination, messageProperties,
                messageType, DestinationType.TOPIC));
        executor.schedule( this::transactionalSend,0, TimeUnit.MICROSECONDS);
    }

    private synchronized void sendOutboxMessages()
    {
        outboxRepository.get().stream()
            .filter(outboxMessage -> outboxMessage.destinationType.equals(DestinationType.QUEUE))
            .forEach(outboxMessage -> {
                sendToQueue(outboxMessage);
                outboxRepository.remove(outboxMessage.messageId());
            });

        outboxRepository.get().stream()
            .filter(outboxMessage -> outboxMessage.destinationType.equals(DestinationType.TOPIC))
            .forEach(outboxMessage -> {
                sendToTopic(outboxMessage);
                outboxRepository.remove(outboxMessage.messageId());
            });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void sendToQueue(JexxaOutboxMessage outboxMessage)
    {
        MessageBuilder producer;
        if (outboxMessage.messageType.equals(MessageType.TEXT_MESSAGE))
        {
            producer = messageSender.send(outboxMessage.message()).toQueue(outboxMessage.destination());
        } else {
            producer = messageSender.sendByteMessage(outboxMessage.message()).toQueue(outboxMessage.destination());
        }
        if (outboxMessage.messageProperties() != null) {
            outboxMessage.messageProperties().forEach((key, value) -> producer.addHeader((String) key, (String) value));
        }
        producer.addHeader("domain_event_id", outboxMessage.messageId().toString()).asString();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void sendToTopic( JexxaOutboxMessage outboxMessage)
    {
        MessageBuilder producer;
        if (outboxMessage.messageType().equals(MessageType.TEXT_MESSAGE))
        {
            producer = messageSender.send(outboxMessage.message()).toTopic(outboxMessage.destination());
        } else {
            producer = messageSender.sendByteMessage(outboxMessage.message()).toTopic(outboxMessage.destination());
        }
        if (outboxMessage.messageProperties() != null) {
            outboxMessage.messageProperties().forEach((key, value) -> producer.addHeader((String) key, (String) value));
        }
        producer.addHeader("domain_event_id", outboxMessage.messageId().toString()).asString();
    }


    enum DestinationType{ TOPIC, QUEUE }

    record JexxaOutboxMessage(UUID messageId, String message, String destination,
                              Properties messageProperties, MessageType messageType,
                              DestinationType destinationType)
    {   }

}

package io.jexxa.common.drivingadapter.messaging.jms.idempotent;

import io.jexxa.common.drivenadapter.persistence.repository.IRepository;
import io.jexxa.common.drivingadapter.messaging.jms.listener.JSONMessageListener;

import javax.jms.JMSException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Properties;

import static io.jexxa.common.drivenadapter.persistence.RepositoryFactory.createRepository;
import static io.jexxa.common.facade.logger.SLF4jLogger.getLogger;

/**
 * This listener implements the idempotent receiver. So this listener can safely receive the same message multiple times.
 * <p>
 * More information about this pattern can be found <a href="https://www.enterpriseintegrationpatterns.com/patterns/messaging/IdempotentReceiver.html"> here </a>
 * @param <T> Type of the received message
 */
public abstract class IdempotentListener<T> extends JSONMessageListener
{
    private static final Duration DEFAULT_STORAGE_DURATION = Duration.ofDays(7);
    private static final String DEFAULT_MESSAGE_ID = "domain_event_id";
    private final IRepository<InboundMessage, ReceivingID> messageRepository;
    private final Class<T> clazz;
    private Instant oldestMessage;
    private int duplicateMessageCounter;

    protected IdempotentListener(Class<T> clazz, Properties properties)
    {
        this.clazz = Objects.requireNonNull( clazz );
        messageRepository = createRepository(InboundMessage.class, InboundMessage::receivingID, properties);
    }
    @Override
    public final void onMessage(String message)
    {
        // If we do not find a uniqueID, we show a warning and process the message
        var uniqueID = uniqueID();
        if ( !messageHeaderIncludes( uniqueID ))
        {
            getLogger(getClass()).warn("Message does not include an ID {} -> Process message", uniqueID);
            onMessage( fromJson(message, clazz ));
            return;
        }

        // If we already processed the ID, we show an info message and return
        var receivingID = new ReceivingID(getMessageHeaderValue(uniqueID), this.getClass().getName());
        if (messageRepository.get(receivingID).isPresent()) {
            getLogger(getClass()).info("Message with key {} already processed by {} -> Ignore it", receivingID.uuid, receivingID.className);
            ++duplicateMessageCounter;
            return;
        }

        onMessage( fromJson(message, clazz ));
        messageRepository.add(new InboundMessage(receivingID, Instant.now()));
        removeOldMessages();
    }

    public int duplicateMessageCounter() {
        return duplicateMessageCounter;
    }

    public abstract void onMessage(T message);

    protected String uniqueID()
    {
        return DEFAULT_MESSAGE_ID;
    }

    protected boolean messageHeaderIncludes(String key)
    {
        try {
            if (getCurrentMessage() != null) {
                return getCurrentMessage().propertyExists(key);
            } else {
                return false;
            }
        } catch (JMSException _)
        {
            return false;
        }
    }

    protected Duration getStorageDuration()
    {
        return DEFAULT_STORAGE_DURATION;
    }

    protected String getMessageHeaderValue(String key)
    {
        try {
            if (getCurrentMessage() != null) {
                return getCurrentMessage().getStringProperty(key);
            }
        } catch (JMSException _)
        {
            return null;
        }
        return null;
    }

    private void removeOldMessages()
    {
        if (!expiredMessageAvailable(oldestMessage))
        {
            return;
        }

        messageRepository.get().stream()
                .filter(this::toBeRemoved)
                .forEach(this::removeMessage);

        oldestMessage = messageRepository.get()
                .stream()
                .map(InboundMessage::processingTime)
                .sorted()
                .findFirst()
                .orElse(null);
    }

    private boolean expiredMessageAvailable(Instant oldestMessage)
    {
        if (oldestMessage == null)
        {
            return true;
        }

        return Duration.between( oldestMessage, Instant.now() ).compareTo(getStorageDuration()) > 0;
    }

    private boolean toBeRemoved(InboundMessage inboundMessage)
    {
        return Duration
                .between( inboundMessage.processingTime, Instant.now())
                .compareTo(getStorageDuration()) >= 0;
    }

    private void removeMessage(InboundMessage inboundMessage)
    {
        try {
            messageRepository.remove(inboundMessage.receivingID);
        } catch (RuntimeException _)
        {
            // If we use this listener in a sharedSubscription, it could happen that a remove fails.
            // Reason ist that we are potentially in a transaction. If we throw an exception, a successfully executed command will be rolled back.
            // Therefore, we just show an error message if an old message is 10x older than the expiration time.
            if (Duration.between( inboundMessage.processingTime, Instant.now())
                    .compareTo(getStorageDuration().multipliedBy(10)) >=0)
            {
                getLogger(getClass()).warn("Could not cleanup inbound messages. There exist messages that are >= 10x older than the allowed storage duration!");
            }
        }
    }

    record InboundMessage(ReceivingID receivingID, Instant processingTime) {}
    record ReceivingID(String uuid, String className){}

}

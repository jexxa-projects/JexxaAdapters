package io.jexxa.common.adapter.messaging.jms;

import io.jexxa.adapterapi.invocation.transaction.TransactionManager;
import io.jexxa.common.adapter.messaging.receive.jms.JMSAdapter;
import io.jexxa.common.adapter.messaging.receive.jms.JMSConfiguration;
import io.jexxa.common.adapter.messaging.receive.jms.listener.JSONMessageListener;
import io.jexxa.common.adapter.messaging.receive.jms.listener.TypedMessageListener;
import io.jexxa.common.adapter.messaging.send.MessageSender;
import io.jexxa.common.adapter.messaging.send.MessageSenderManager;
import io.jexxa.common.adapter.messaging.send.jms.JMSSender;
import io.jexxa.common.adapter.outbox.TransactionalOutboxSender;
import io.jexxa.common.adapter.outbox.listener.IdempotentListener;
import io.jexxa.common.facade.TestConstants;
import io.jexxa.common.facade.testapplication.JexxaDomainEvent;
import io.jexxa.common.facade.testapplication.JexxaValueObject;
import io.jexxa.common.facade.utils.properties.PropertiesUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static io.jexxa.adapterapi.invocation.DefaultInvocationHandler.GLOBAL_SYNCHRONIZATION_OBJECT;
import static io.jexxa.common.adapter.messaging.jms.QueueListener.QUEUE_DESTINATION;
import static io.jexxa.common.adapter.messaging.jms.TopicListener.TOPIC_DESTINATION;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
@Tag(TestConstants.INTEGRATION_TEST)
class MessageListenerIT
{
    private static final String MESSAGE_SENDER_CONFIG = "getMessageSenderConfig";
    private final JexxaValueObject message = new JexxaValueObject(42);
    private final JexxaDomainEvent domainEvent = JexxaDomainEvent.create(message);


    private JexxaValueObjectListener typedListener;
    private TextMessageListener jsonMessageListener;
    private JexxaValueObjectIdempotentListener idempotentListener;
    private Properties jmsProperties;
    private JMSAdapter jmsAdapter;


    @BeforeEach
    void initTests() throws IOException {
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/application.properties"));
        jmsProperties = PropertiesUtils.getSubset(properties,"test-jms-connection");

        jsonMessageListener = new TextMessageListener();
        typedListener = new JexxaValueObjectListener();
        idempotentListener = new JexxaValueObjectIdempotentListener(jmsProperties);

        jmsAdapter = new JMSAdapter(jmsProperties);
        jmsAdapter.register(jsonMessageListener);
        jmsAdapter.register(typedListener);
        jmsAdapter.register(idempotentListener);
        jmsAdapter.start();
    }

    @AfterEach
    void afterEach() {
        jmsAdapter.stop();
    }

    @SuppressWarnings("unused")
    static Stream<Class<? extends MessageSender>> getMessageSenderConfig()
    {
        return Stream.of(JMSSender.class, TransactionalOutboxSender.class);
    }

    @ParameterizedTest
    @MethodSource(MESSAGE_SENDER_CONFIG)
    void receiveDomainEvent(Class<? extends MessageSender> messageSender)
    {
        //Arrange
        MessageSenderManager.setDefaultStrategy(messageSender);
        var objectUnderTest = MessageSenderManager.getMessageSender(JMSSenderIT.class, jmsProperties);

        //Act
        objectUnderTest
                .send(domainEvent)
                .toTopic(TOPIC_DESTINATION)
                .asJson();

        //Assert
        await().atMost(1, TimeUnit.SECONDS).until(() -> jsonMessageListener.getTextMessage() != null);

    }

    @ParameterizedTest
    @MethodSource(MESSAGE_SENDER_CONFIG)
    void receiveTypedMessage(Class<? extends MessageSender> messageSender)
    {
        //Arrange
        MessageSenderManager.setDefaultStrategy(messageSender);
        var objectUnderTest = MessageSenderManager.getMessageSender(JMSSenderIT.class, jmsProperties);

        //Act
        objectUnderTest
                .send(message)
                .toQueue(QUEUE_DESTINATION)
                .asJson();

        //Assert
        await().atMost(1, TimeUnit.SECONDS).until(() -> typedListener.getJexxaValueObject() != null);
        assertEquals(message, typedListener.getJexxaValueObject());
    }

    @Test
    void validateIdempotentMessaging()
    {
        //Arrange
        MessageSenderManager.setDefaultStrategy(JMSSender.class);
        var objectUnderTest = MessageSenderManager.getMessageSender(JMSSenderIT.class, jmsProperties);
        UUID uuid = UUID.randomUUID();

        //Act
        objectUnderTest
                .send(message)
                .toTopic(TOPIC_DESTINATION)
                .addHeader("domain_event_id", uuid.toString())
                .asJson();

        objectUnderTest
                .send(message)
                .toTopic(TOPIC_DESTINATION)
                .addHeader("domain_event_id", uuid.toString())
                .asJson();

        //Assert - 2 Messages must be received from jsonMessageListener but only one from idempotentListener due to same ID
        await().atMost(2, TimeUnit.SECONDS).until(() -> jsonMessageListener.getReceivedMessages().size() == 2);
        assertEquals(1, idempotentListener.getReceivedMessages().size());
    }

    @Test
    void stressTestIdempotentMessaging()
    {
        //Arrange
        int messageCount = 100;
        var objectUnderTest = MessageSenderManager.getMessageSender(JMSSenderIT.class, jmsProperties);

        //Act
        for (int i = 0; i< messageCount; ++i) {
            synchronized (GLOBAL_SYNCHRONIZATION_OBJECT)
            {
                TransactionManager.initTransaction();
                objectUnderTest
                        .send(message)
                        .toTopic(TOPIC_DESTINATION)
                        .addHeader("domain_event_id", UUID.randomUUID().toString())
                        .asJson();
                TransactionManager.closeTransaction();
            }
        }


        //Assert
        await().atMost(15, TimeUnit.SECONDS).until(() -> idempotentListener.getReceivedMessages().size() == messageCount);
    }


    private static class TextMessageListener extends JSONMessageListener
    {
        private final List<String> receivedMessages = new ArrayList<>();

        private String textMessage;

        @SuppressWarnings("unused")
        @Override
        @JMSConfiguration(destination = TOPIC_DESTINATION, messagingType = JMSConfiguration.MessagingType.TOPIC)
        public void onMessage(String textMessage)
        {
            this.textMessage = textMessage;
            receivedMessages.add(textMessage);
        }

        public String getTextMessage()
        {
            return textMessage;
        }
        public List<String> getReceivedMessages() {
            return receivedMessages;
        }

    }

    private static class JexxaValueObjectListener extends TypedMessageListener<JexxaValueObject>
    {
        private JexxaValueObject jexxaValueObject;

        public JexxaValueObjectListener()
        {
            super(JexxaValueObject.class);
        }

        @SuppressWarnings("unused")
        @Override
        @JMSConfiguration(destination = QUEUE_DESTINATION, messagingType = JMSConfiguration.MessagingType.QUEUE)
        public void onMessage(JexxaValueObject jexxaValueObject)
        {
            assertTrue(messageContains("valueInPercent"));
            this.jexxaValueObject = jexxaValueObject;
        }

        public JexxaValueObject getJexxaValueObject()
        {
            return jexxaValueObject;
        }

    }

    private static class JexxaValueObjectIdempotentListener extends IdempotentListener<JexxaDomainEvent>
    {
        private final List<JexxaDomainEvent> receivedMessages = new ArrayList<>();

        protected JexxaValueObjectIdempotentListener(Properties properties) {
            super(JexxaDomainEvent.class, properties);
        }

        @Override
        @JMSConfiguration(destination = TOPIC_DESTINATION, messagingType = JMSConfiguration.MessagingType.TOPIC)
        public void onMessage(JexxaDomainEvent message) {
            receivedMessages.add(message);
        }

        public List<JexxaDomainEvent> getReceivedMessages() {
            return receivedMessages;
        }
    }

}

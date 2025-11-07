package io.jexxa.common.drivenadapter.outbox;

import io.jexxa.adapterapi.JexxaContext;
import io.jexxa.common.drivenadapter.messaging.jms.JMSSender;
import io.jexxa.common.drivenadapter.persistence.RepositoryFactory;
import io.jexxa.common.drivingadapter.messaging.jms.JMSAdapter;
import io.jexxa.common.drivingadapter.messaging.jms.JMSConfiguration;
import io.jexxa.common.drivingadapter.messaging.jms.idempotent.IdempotentListener;
import io.jexxa.common.facade.testapplication.TestDomainEvent;
import io.jexxa.common.facade.testapplication.TestValueObject;
import io.jexxa.common.facade.utils.properties.PropertiesUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.jexxa.common.drivenadapter.messaging.MessageSenderFactory.createMessageSender;
import static io.jexxa.common.drivenadapter.messaging.MessageSenderFactory.setDefaultMessageSender;
import static io.jexxa.common.drivingadapter.messaging.jms.listener.TopicListener.TOPIC_DESTINATION;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionalOutboxSenderIT {

    private final TestValueObject message = new TestValueObject(42);

    private ValueObjectIdempotentListener1 idempotentListener;
    private Properties outboxProperties1;
    private Properties outboxProperties2;

    private JMSAdapter jmsAdapter1;


    @BeforeEach
    void initTests() throws IOException {
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/application.properties"));
        outboxProperties1 = PropertiesUtils.filterByPrefix(properties,"test-outbox-connection1");
        outboxProperties2 = PropertiesUtils.filterByPrefix(properties,"test-outbox-connection2");

        idempotentListener = new ValueObjectIdempotentListener1(outboxProperties1);

        jmsAdapter1 = new JMSAdapter(outboxProperties1);
        jmsAdapter1.register(idempotentListener);
        jmsAdapter1.start();

        setDefaultMessageSender(TransactionalOutboxSender.class);
        JexxaContext.init();
        RepositoryFactory.defaultSettings();
    }

    @AfterEach
    void afterEach() {
        jmsAdapter1.stop();
        JexxaContext.cleanup();
    }
    @Test
    void validateIdempotentHandlingOfDuplicateMessages()
    {
        //Arrange
        setDefaultMessageSender(JMSSender.class);// To enforce sending a message twice, we use JMSSender

        var objectUnderTest = createMessageSender(TransactionalOutboxSenderIT.class, outboxProperties1);
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

        //Assert - 2 Messages must be received from jsonMessageListener but only one from idempotentListener due to the same ID
        await().atMost(2, TimeUnit.SECONDS).until(() -> idempotentListener.duplicateMessageCounter() == 1);
        assertEquals(1, idempotentListener.getReceivedMessages().size());
    }

    @Test
    void testTwoOutboxSender()
    {
        //Arrange
        try (JMSAdapter jmsAdapter2 = new JMSAdapter(outboxProperties1)) {
            var idempotentListener2 = new ValueObjectIdempotentListener2(outboxProperties2);

            jmsAdapter2.register(idempotentListener2);
            jmsAdapter2.start();


            var objectUnderTest1 = createMessageSender(TransactionalOutboxSenderIT.class, outboxProperties1);
            var objectUnderTest2 = createMessageSender(TransactionalOutboxSenderIT.class, outboxProperties1);

            //Act
            for (int i = 0; i < 100; ++i ) {
                objectUnderTest1
                        .send(message)
                        .toTopic(TOPIC_DESTINATION)
                        .asJson();

                objectUnderTest2
                        .send(message)
                        .toTopic("JEXXA_TOPIC2")
                        .asJson();
            }
            //Assert
            await().atMost(15, TimeUnit.SECONDS).until(() -> idempotentListener.getReceivedMessages().size() == 100);
            await().atMost(15, TimeUnit.SECONDS).until(() -> idempotentListener2.getReceivedMessages().size() == 100);
            assertEquals(0, idempotentListener.duplicateMessageCounter());
            assertEquals(0, idempotentListener2.duplicateMessageCounter());
        }
    }

    @Test
    void stressTestIdempotentMessaging()
    {
        //Arrange
        int messageCount = 100;
        var objectUnderTest = createMessageSender(TransactionalOutboxSenderIT.class, outboxProperties1);

        //Act
        for (int i = 0; i< messageCount; ++i) {
            UUID uuid = UUID.randomUUID();

            objectUnderTest
                    .send(message)
                    .toTopic(TOPIC_DESTINATION)
                    .addHeader("domain_event_id", uuid.toString())
                    .asJson();
        }


        //Assert
        await().atMost(15, TimeUnit.SECONDS).until(() -> idempotentListener.getReceivedMessages().size() == messageCount);
    }

    private static class ValueObjectIdempotentListener1 extends IdempotentListener<TestDomainEvent>
    {
        private final List<TestDomainEvent> receivedMessages = new ArrayList<>();

        protected ValueObjectIdempotentListener1(Properties properties) {
            super(TestDomainEvent.class, properties);
        }

        @Override
        @JMSConfiguration(destination = TOPIC_DESTINATION, messagingType = JMSConfiguration.MessagingType.TOPIC)
        public void onMessage(TestDomainEvent message) {
            receivedMessages.add(message);
        }

        public List<TestDomainEvent> getReceivedMessages() {
            return receivedMessages;
        }
    }

    private static class ValueObjectIdempotentListener2 extends IdempotentListener<TestDomainEvent>
    {
        private final List<TestDomainEvent> receivedMessages = new ArrayList<>();

        protected ValueObjectIdempotentListener2(Properties properties) {
            super(TestDomainEvent.class, properties);
        }

        @Override
        @JMSConfiguration(destination = "JEXXA_TOPIC2", messagingType = JMSConfiguration.MessagingType.TOPIC)
        public void onMessage(TestDomainEvent message) {
            receivedMessages.add(message);
        }

        public List<TestDomainEvent> getReceivedMessages() {
            return receivedMessages;
        }
    }
}

package io.jexxa.common.drivenadapter.outbox;

import io.jexxa.adapterapi.invocation.transaction.TransactionManager;
import io.jexxa.common.drivingadapter.messaging.jms.JMSAdapter;
import io.jexxa.common.drivingadapter.messaging.jms.JMSConfiguration;
import io.jexxa.common.drivenadapter.messaging.MessageSenderManager;
import io.jexxa.common.drivenadapter.messaging.jms.JMSSender;
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

import static io.jexxa.adapterapi.invocation.DefaultInvocationHandler.GLOBAL_SYNCHRONIZATION_OBJECT;
import static io.jexxa.common.drivingadapter.messaging.jms.listener.TopicListener.TOPIC_DESTINATION;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionalOutboxSenderIT {

    private final TestValueObject message = new TestValueObject(42);

    private ValueObjectIdempotentListener idempotentListener;
    private Properties jmsProperties;
    private JMSAdapter jmsAdapter;


    @BeforeEach
    void initTests() throws IOException {
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/application.properties"));
        jmsProperties = PropertiesUtils.getSubset(properties,"test-jms-connection");

        idempotentListener = new ValueObjectIdempotentListener(jmsProperties);

        jmsAdapter = new JMSAdapter(jmsProperties);
        jmsAdapter.register(idempotentListener);
        jmsAdapter.start();
    }

    @AfterEach
    void afterEach() {
        jmsAdapter.stop();
    }
    @Test
    void validateIdempotentMessaging()
    {
        //Arrange
        MessageSenderManager.setDefaultStrategy(JMSSender.class);
        var objectUnderTest = MessageSenderManager.getMessageSender(TransactionalOutboxSenderIT.class, jmsProperties);
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
    void stressTestIdempotentMessaging()
    {
        //Arrange
        int messageCount = 100;
        var objectUnderTest = MessageSenderManager.getMessageSender(TransactionalOutboxSenderIT.class, jmsProperties);

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

    private static class ValueObjectIdempotentListener extends IdempotentListener<TestDomainEvent>
    {
        private final List<TestDomainEvent> receivedMessages = new ArrayList<>();

        protected ValueObjectIdempotentListener(Properties properties) {
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
}

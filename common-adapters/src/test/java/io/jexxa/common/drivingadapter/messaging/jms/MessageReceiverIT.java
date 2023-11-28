package io.jexxa.common.drivingadapter.messaging.jms;

import io.jexxa.common.drivingadapter.messaging.jms.listener.JSONMessageListener;
import io.jexxa.common.drivingadapter.messaging.jms.listener.TypedMessageListener;
import io.jexxa.common.drivenadapter.messaging.MessageSender;
import io.jexxa.common.drivenadapter.messaging.MessageSenderManager;
import io.jexxa.common.drivenadapter.messaging.jms.JMSSender;
import io.jexxa.common.drivenadapter.outbox.TransactionalOutboxSender;
import io.jexxa.common.facade.TestConstants;
import io.jexxa.common.facade.testapplication.TestDomainEvent;
import io.jexxa.common.facade.testapplication.TestValueObject;
import io.jexxa.common.facade.utils.properties.PropertiesUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static io.jexxa.common.drivingadapter.messaging.jms.listener.QueueListener.QUEUE_DESTINATION;
import static io.jexxa.common.drivingadapter.messaging.jms.listener.TopicListener.TOPIC_DESTINATION;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
@Tag(TestConstants.INTEGRATION_TEST)
class MessageReceiverIT
{
    private static final String MESSAGE_SENDER_CONFIG = "getMessageSenderConfig";
    private final TestValueObject message = new TestValueObject(42);
    private final TestDomainEvent domainEvent = TestDomainEvent.create(message);


    private ValueObjectListener typedListener;
    private TextMessageListener jsonMessageListener;
    private Properties jmsProperties;
    private JMSAdapter jmsAdapter;


    @BeforeEach
    void initTests() throws IOException {
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/application.properties"));
        jmsProperties = PropertiesUtils.getSubset(properties,"test-jms-connection");

        jsonMessageListener = new TextMessageListener();
        typedListener = new ValueObjectListener();

        jmsAdapter = new JMSAdapter(jmsProperties);
        jmsAdapter.register(jsonMessageListener);
        jmsAdapter.register(typedListener);
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
        var objectUnderTest = MessageSenderManager.getMessageSender(MessageReceiverIT.class, jmsProperties);

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
        var objectUnderTest = MessageSenderManager.getMessageSender(MessageReceiverIT.class, jmsProperties);

        //Act
        objectUnderTest
                .send(message)
                .toQueue(QUEUE_DESTINATION)
                .asJson();

        //Assert
        await().atMost(1, TimeUnit.SECONDS).until(() -> typedListener.valueObject() != null);
        assertEquals(message, typedListener.valueObject());
    }




    private static class TextMessageListener extends JSONMessageListener
    {
        private String textMessage;

        @SuppressWarnings("unused")
        @Override
        @JMSConfiguration(destination = TOPIC_DESTINATION, messagingType = JMSConfiguration.MessagingType.TOPIC)
        public void onMessage(String textMessage)
        {
            this.textMessage = textMessage;
        }

        public String getTextMessage()
        {
            return textMessage;
        }
    }

    private static class ValueObjectListener extends TypedMessageListener<TestValueObject>
    {
        private TestValueObject testValueObject;

        public ValueObjectListener()
        {
            super(TestValueObject.class);
        }

        @SuppressWarnings("unused")
        @Override
        @JMSConfiguration(destination = QUEUE_DESTINATION, messagingType = JMSConfiguration.MessagingType.QUEUE)
        public void onMessage(TestValueObject testValueObject)
        {
            assertTrue(messageContains("valueInPercent"));
            this.testValueObject = testValueObject;
        }

        public TestValueObject valueObject()
        {
            return testValueObject;
        }

    }


}

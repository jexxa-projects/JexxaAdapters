package io.jexxa.adapters.jms.drivngadapter;



import io.jexxa.adapters.api.JexxaContext;
import io.jexxa.adapters.jms.drivenadapter.JMSSender;
import io.jexxa.adapters.jms.drivenadapter.TransactionalOutboxSender;
import io.jexxa.adapters.jms.drivingadapter.JMSAdapter;
import io.jexxa.adapters.jms.drivingadapter.JMSConfiguration;
import io.jexxa.adapters.jms.drivingadapter.listener.JSONMessageListener;
import io.jexxa.adapters.jms.drivingadapter.listener.TypedMessageListener;
import io.jexxa.adapters.testapplication.TestConstants;
import io.jexxa.adapters.testapplication.TestDomainEvent;
import io.jexxa.adapters.testapplication.TestValueObject;
import io.jexxa.adapters.core.drivenadapter.messaging.MessageSender;
import io.jexxa.adapters.core.facade.utils.properties.PropertiesUtils;
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

import static io.jexxa.adapters.jms.drivngadapter.listener.QueueListener.QUEUE_DESTINATION;
import static io.jexxa.adapters.jms.drivngadapter.listener.TopicListener.TOPIC_DESTINATION;
import static io.jexxa.adapters.core.drivenadapter.messaging.MessageSenderFactory.createMessageSender;
import static io.jexxa.adapters.core.drivenadapter.messaging.MessageSenderFactory.setDefaultMessageSender;
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
        jmsProperties = PropertiesUtils.filterByPrefix(properties,"test-jms-connection");

        jsonMessageListener = new TextMessageListener();
        typedListener = new ValueObjectListener();

        jmsAdapter = new JMSAdapter(jmsProperties);
        jmsAdapter.register(jsonMessageListener);
        jmsAdapter.register(typedListener);
        jmsAdapter.start();
        JexxaContext.init();
    }

    @AfterEach
    void afterEach() {
        jmsAdapter.stop();
        JexxaContext.cleanup();
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
        setDefaultMessageSender(messageSender);
        var objectUnderTest = createMessageSender(MessageReceiverIT.class, jmsProperties);

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
        setDefaultMessageSender(messageSender);
        var objectUnderTest = createMessageSender(MessageReceiverIT.class, jmsProperties);

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

package io.jexxa.commons.adapter.messaging.jms;

import io.jexxa.commons.adapter.messaging.receive.jms.JMSAdapter;
import io.jexxa.commons.adapter.messaging.receive.jms.JMSConfiguration;
import io.jexxa.commons.adapter.messaging.receive.jms.listener.JSONMessageListener;
import io.jexxa.commons.adapter.messaging.receive.jms.listener.TypedMessageListener;
import io.jexxa.commons.adapter.messaging.send.MessageSender;
import io.jexxa.commons.adapter.messaging.send.MessageSenderFactory;
import io.jexxa.commons.adapter.messaging.send.jms.JMSSender;
import io.jexxa.commons.facade.TestConstants;
import io.jexxa.commons.facade.testapplication.JexxaDomainEvent;
import io.jexxa.commons.facade.testapplication.JexxaValueObject;
import io.jexxa.commons.facade.utils.properties.PropertiesUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static io.jexxa.commons.adapter.messaging.jms.QueueListener.QUEUE_DESTINATION;
import static io.jexxa.commons.adapter.messaging.jms.TopicListener.TOPIC_DESTINATION;
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
    private Properties jmsProperties;
    private JMSAdapter jmsAdapter;


    @BeforeEach
    void initTests() throws IOException {
        jsonMessageListener = new TextMessageListener();
        typedListener = new JexxaValueObjectListener();

        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/application.properties"));
        jmsProperties = PropertiesUtils.getSubset(properties,"test-jms-connection");

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
        return Stream.of(JMSSender.class);
    }

    @ParameterizedTest
    @MethodSource(MESSAGE_SENDER_CONFIG)
    void receiveDomainEvent(Class<? extends MessageSender> messageSender)
    {
        //Arrange
        MessageSenderFactory.setDefaultStrategy(messageSender);
        var objectUnderTest = MessageSenderFactory.getMessageSender(JMSSenderIT.class, jmsProperties);

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
        MessageSenderFactory.setDefaultStrategy(messageSender);
        var objectUnderTest = MessageSenderFactory.getMessageSender(JMSSenderIT.class, jmsProperties);

        //Act
        objectUnderTest
                .send(message)
                .toQueue(QUEUE_DESTINATION)
                .asJson();

        //Assert
        await().atMost(1, TimeUnit.SECONDS).until(() -> typedListener.getJexxaValueObject() != null);
        assertEquals(message, typedListener.getJexxaValueObject());
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



}

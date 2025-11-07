package io.jexxa.common.drivenadapter.messaging.jms;


import io.jexxa.adapterapi.JexxaContext;
import io.jexxa.common.drivenadapter.messaging.MessageSender;
import io.jexxa.common.drivingadapter.messaging.jms.JMSAdapter;
import io.jexxa.common.drivingadapter.messaging.jms.listener.QueueListener;
import io.jexxa.common.drivingadapter.messaging.jms.listener.TopicListener;
import io.jexxa.common.facade.TestConstants;
import io.jexxa.common.facade.testapplication.TestValueObject;
import io.jexxa.common.facade.utils.properties.PropertiesUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static io.jexxa.common.drivenadapter.messaging.MessageSenderFactory.createMessageSender;
import static io.jexxa.common.drivenadapter.messaging.MessageSenderFactory.setDefaultMessageSender;
import static io.jexxa.common.drivingadapter.messaging.jms.listener.QueueListener.QUEUE_DESTINATION;
import static io.jexxa.common.drivingadapter.messaging.jms.listener.TopicListener.TOPIC_DESTINATION;
import static io.jexxa.common.facade.jms.JMSProperties.jndiPasswordFile;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTimeout;

@Execution(ExecutionMode.SAME_THREAD)
@Tag(TestConstants.INTEGRATION_TEST)
class JMSSenderIT
{
    private static final String MESSAGE_SENDER_CONFIG = "getMessageSenderConfig";
    private static final String TYPE = "type";
    private final TestValueObject message = new TestValueObject(42);

    private TopicListener topicListener;
    private QueueListener queueListener;
    private Properties jmsProperties;
    JMSAdapter jmsAdapter;

    @BeforeEach
    void initTests() throws IOException {
        topicListener = new TopicListener();
        queueListener = new QueueListener();

        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/application.properties"));
        jmsProperties = PropertiesUtils.filterByPrefix(properties,"test-jms-connection");

        jmsAdapter = new JMSAdapter(jmsProperties);
        jmsAdapter.register(queueListener);
        jmsAdapter.register(topicListener);
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
        return Stream.of(JMSSender.class);
    }

    @ParameterizedTest
    @MethodSource(MESSAGE_SENDER_CONFIG)
    void sendMessageToTopic(Class<? extends MessageSender> messageSender)
    {
        //Arrange
        setDefaultMessageSender(messageSender);
        var objectUnderTest = createMessageSender(JMSSenderIT.class, jmsProperties);

        //Act
        objectUnderTest
                .send(message)
                .toTopic(TOPIC_DESTINATION)
                .addHeader(TYPE, message.getClass().getSimpleName())
                .asJson();

        //Assert
        await().atMost(1, TimeUnit.SECONDS).until(() -> !topicListener.getMessages().isEmpty());

        assertDoesNotThrow(() -> (TextMessage)topicListener.getMessages().get(0));

        assertTimeout(Duration.ofSeconds(5), jmsAdapter::stop);
    }


    @ParameterizedTest
    @MethodSource(MESSAGE_SENDER_CONFIG)
    void sendMessageToQueue(Class<? extends MessageSender> messageSender)
    {
        //Arrange
        setDefaultMessageSender(messageSender);
        var objectUnderTest = createMessageSender(JMSSenderIT.class, jmsProperties);

        //Act
        objectUnderTest
                .send(message)
                .toQueue(QUEUE_DESTINATION)
                .addHeader(TYPE, message.getClass().getSimpleName())
                .asJson();

        //Assert
        await().atMost(1, TimeUnit.SECONDS).until(() -> !queueListener.getMessages().isEmpty());
        assertDoesNotThrow(() -> (TextMessage)queueListener.getMessages().get(0));

    }

    @ParameterizedTest
    @MethodSource(MESSAGE_SENDER_CONFIG)
    void sendMessageToQueueAsString(Class<? extends MessageSender> messageSender)
    {
        //Arrange
        setDefaultMessageSender(messageSender);
        var objectUnderTest = createMessageSender(JMSSenderIT.class, jmsProperties);


        //Act
        objectUnderTest
                .send(message)
                .toQueue(QUEUE_DESTINATION)
                .addHeader(TYPE, message.getClass().getSimpleName())
                .asString();

        //Assert
        await().atMost(1, TimeUnit.SECONDS).until(() -> !queueListener.getMessages().isEmpty());
        assertDoesNotThrow(() -> (TextMessage)queueListener.getMessages().get(0));

    }


    @ParameterizedTest
    @MethodSource(MESSAGE_SENDER_CONFIG)
    void sendByteMessageToTopic(Class<? extends MessageSender> messageSender)
    {
        //Arrange
        setDefaultMessageSender(messageSender);
        var objectUnderTest = createMessageSender(JMSSenderIT.class, jmsProperties);


        //Act
        objectUnderTest
                .sendByteMessage(message)
                .toTopic(TOPIC_DESTINATION)
                .addHeader(TYPE, message.getClass().getSimpleName())
                .asJson();

        //Assert
        await().atMost(1, TimeUnit.SECONDS).until(() -> !topicListener.getMessages().isEmpty());

        assertDoesNotThrow(() -> (BytesMessage)topicListener.getMessages().get(0));
    }

    @ParameterizedTest
    @MethodSource(MESSAGE_SENDER_CONFIG)
    void sendByteMessageToQueue(Class<? extends MessageSender> messageSender)
    {
        //Arrange
        setDefaultMessageSender(messageSender);
        var objectUnderTest = createMessageSender(JMSSenderIT.class, jmsProperties);


        //Act
        objectUnderTest
                .sendByteMessage(message)
                .toQueue(QUEUE_DESTINATION)
                .addHeader(TYPE, message.getClass().getSimpleName())
                .asJson();

        //Assert
        await().atMost(1, TimeUnit.SECONDS).until(() -> !queueListener.getMessages().isEmpty());

        assertDoesNotThrow(() -> (BytesMessage)queueListener.getMessages().get(0));
    }

    @Test
    void sendMessageReconnectQueue() throws JMSException
    {
        //Arrange
        setDefaultMessageSender(JMSSender.class); // Reconnect is only meaningful for JMSSender
        var objectUnderTest = createMessageSender(JMSSenderIT.class, jmsProperties);

        //Act (simulate an error in between sending two messages
        objectUnderTest
                .send(message)
                .toQueue(QUEUE_DESTINATION)
                .asJson();

        //Simulate the error
        simulateConnectionException(((JMSSender) (objectUnderTest)).getConnection());

        objectUnderTest
                .send(message)
                .toQueue(QUEUE_DESTINATION)
                .asJson();

        //Assert
        await().atMost(1, TimeUnit.SECONDS).until(() -> queueListener.getMessages().size() >= 2);
        assertDoesNotThrow(() -> (TextMessage)queueListener.getMessages().get(0));

    }

    @Test
    void testPasswordFile()
    {
        //Arrange
        var properties = new Properties();
        properties.putAll(jmsProperties);
        properties.remove(jndiPasswordFile());
        properties.put(jndiPasswordFile(), "src/test/resources/secrets/jndiPassword");
        setDefaultMessageSender(JMSSender.class);

        var objectUnderTest = createMessageSender(JMSSenderIT.class, properties);

        //Act
        objectUnderTest
                .sendByteMessage(message)
                .toQueue(QUEUE_DESTINATION)
                .addHeader(TYPE, message.getClass().getSimpleName())
                .asJson();

        //Assert
        await().atMost(1, TimeUnit.SECONDS).until(() -> !queueListener.getMessages().isEmpty());

        assertDoesNotThrow(() -> (BytesMessage)queueListener.getMessages().get(0));
    }

    private void simulateConnectionException(Connection connection) throws JMSException
    {
        var listener = connection.getExceptionListener();

        connection.close();

        listener.onException(new JMSException("Simulated error "));
    }
}
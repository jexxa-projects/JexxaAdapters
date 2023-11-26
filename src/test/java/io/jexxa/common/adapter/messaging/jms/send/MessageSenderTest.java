package io.jexxa.common.adapter.messaging.jms.send;

import io.jexxa.common.adapter.messaging.send.MessageBuilder;
import io.jexxa.common.adapter.messaging.send.MessageSender;
import io.jexxa.common.facade.testapplication.JexxaValueObject;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static io.jexxa.common.facade.json.JSONManager.getJSONConverter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageSenderTest
{

    @Test
    void testMessageAsText()
    {
        //Arrange
        var localMessageSender = new LocalMessageSender();
        var testData = new JexxaValueObject(42);
        var objectUnderTest = localMessageSender.send(testData);

        //Act
        objectUnderTest.toQueue("TestQueue")
                .asString();

        //Assertions
        assertNotNull(localMessageSender.getMessage());
        assertEquals(MessageBuilder.DestinationType.QUEUE, localMessageSender.getDestinationType());

        assertEquals(testData.toString(), localMessageSender.getMessage());
    }

    @Test
    void testMessageAsGson()
    {
        //Arrange
        var localMessageSender = new LocalMessageSender();
        var testData = new JexxaValueObject(42);
        var objectUnderTest = localMessageSender.send(testData);

        //Act
        objectUnderTest.toQueue("TestQueue")
                .asJson();

        //Assertions
        assertNotNull(localMessageSender.getMessage());
        assertEquals(MessageBuilder.DestinationType.QUEUE, localMessageSender.getDestinationType());

        assertEquals(getJSONConverter().toJson(testData), localMessageSender.getMessage());
    }

    @Test
    void testInvalidMessageProducerUsage()
    {
        //Arrange
        var localMessageSender = new LocalMessageSender();
        var objectUnderTest = localMessageSender.send(new Object());

        //No destination set
        assertThrows( NullPointerException.class, objectUnderTest::asString);
    }



    private static class LocalMessageSender extends MessageSender
    {
        private String message;
        private MessageBuilder.DestinationType destinationType = null;

        @Override
        protected void sendToQueue(String message, String destination, Properties messageProperties, MessageType messageType)
        {
            this.message = message;
            this.destinationType = MessageBuilder.DestinationType.QUEUE;
        }

        @Override
        protected void sendToTopic(String message, String destination, Properties messageProperties, MessageType messageType)
        {
            this.message = message;
            this.destinationType = MessageBuilder.DestinationType.TOPIC;
        }

        String getMessage()
        {
            return message;
        }

        MessageBuilder.DestinationType getDestinationType()
        {
            return destinationType;
        }

    }
}
package io.jexxa.common.messaging;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InProcessPubSubTest
{
    @Test
    void publishMessageToSubscriber()
    {
        // Arrange
        var pubSub = new InProcessPubSub();
        var receivedMessages = new ArrayList<String>();

        pubSub.subscribe(String.class, receivedMessages::add);

        // Act
        pubSub.publish("Hello World");

        // Assert
        assertEquals(List.of("Hello World"), receivedMessages);
    }

    @Test
    void publishOnlyToMatchingSubscribers()
    {
        // Arrange
        var pubSub = new InProcessPubSub();
        var receivedStrings = new ArrayList<String>();
        var receivedIntegers = new ArrayList<Integer>();

        pubSub.subscribe(String.class, receivedStrings::add);
        pubSub.subscribe(Integer.class, receivedIntegers::add);

        // Act
        pubSub.publish("Hello World");

        // Assert
        assertEquals(List.of("Hello World"), receivedStrings);
        assertTrue(receivedIntegers.isEmpty());
    }

    @Test
    void publishMessageToMultipleSubscribers()
    {
        // Arrange
        var pubSub = new InProcessPubSub();
        var firstSubscriber = new ArrayList<String>();
        var secondSubscriber = new ArrayList<String>();

        pubSub.subscribe(String.class, firstSubscriber::add);
        pubSub.subscribe(String.class, secondSubscriber::add);

        // Act
        pubSub.publish("Hello World");

        // Assert
        assertEquals(List.of("Hello World"), firstSubscriber);
        assertEquals(List.of("Hello World"), secondSubscriber);
    }

    @Test
    void subscribeToAllMessageTypes()
    {
        // Arrange
        var pubSub = new InProcessPubSub();
        var receivedMessages = new ArrayList<>();

        pubSub.subscribe(receivedMessages::add);

        // Act
        pubSub.publish("Hello World");
        pubSub.publish(42);

        // Assert
        assertEquals(List.of("Hello World", 42), receivedMessages);
    }

    @Test
    void publishMessageToSubscriberOfSupertype()
    {
        // Arrange
        var pubSub = new InProcessPubSub();
        var receivedMessages = new ArrayList<Number>();

        pubSub.subscribe(Number.class, receivedMessages::add);

        // Act
        pubSub.publish(42);

        // Assert
        assertEquals(List.of(42), receivedMessages);
    }
}
package io.jexxa.adapters.core.drivenadapter.messaging;

import io.jexxa.adapters.core.messaging.InProcessPubSub;

import java.util.Properties;

public class InProcessMessageSender extends MessageSender {
    @Override
    protected void sendToQueue(String message, String destination, Properties messageProperties, MessageType messageType) {
        InProcessPubSub.instance().publish(destination, message);
    }

    @Override
    protected void sendToTopic(String message, String destination, Properties messageProperties, MessageType messageType) {
        InProcessPubSub.instance().publish(destination, destination);
    }
}

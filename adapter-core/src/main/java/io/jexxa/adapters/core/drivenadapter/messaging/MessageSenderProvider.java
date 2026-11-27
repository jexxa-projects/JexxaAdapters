package io.jexxa.adapters.core.drivenadapter.messaging;

import java.util.Optional;
import java.util.Properties;

public interface MessageSenderProvider
{
    Optional<Class<? extends MessageSender>> getMessageSenderType(
            Properties properties);
}
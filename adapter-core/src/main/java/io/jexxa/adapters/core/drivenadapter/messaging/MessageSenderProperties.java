package io.jexxa.adapters.core.drivenadapter.messaging;

import io.jexxa.adapters.core.facade.utils.properties.PropertiesPrefix;

@SuppressWarnings("unused")
public class MessageSenderProperties {
    private MessageSenderProperties() {
        /* This utility class should not be instantiated */
    }

    public static String messageSenderStrategy() { return PropertiesPrefix.globalPrefix() + "jms.strategy"; }
    public static String messageSenderSimulate() { return PropertiesPrefix.globalPrefix() + "jms.simulate"; }

}

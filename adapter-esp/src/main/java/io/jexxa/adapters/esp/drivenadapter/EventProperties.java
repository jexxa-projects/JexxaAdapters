package io.jexxa.adapters.esp.drivenadapter;


import static io.jexxa.adapters.core.facade.utils.properties.PropertiesPrefix.globalPrefix;

public class EventProperties {
    public static String eventStrategy() { return globalPrefix() + "event.strategy"; }
    private EventProperties() {

    }
}

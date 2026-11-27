package io.jexxa.adapters.esp.drivenadapter;


import io.jexxa.adapters.api.JexxaContext;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static io.jexxa.adapters.esp.drivenadapter.EventSenderFactory.createEventSender;
import static io.jexxa.adapters.esp.drivenadapter.KafkaPool.validateKafkaConnection;

@SuppressWarnings("java:S6548")
public class EventSenderPool {
    private static final EventSenderPool INSTANCE = new EventSenderPool();

    private final Map<Object, EventSender> messageSenderMap = Collections.synchronizedMap(new ConcurrentHashMap<>());

    public static EventSender eventSender(Object managingObject, Properties properties)
    {
        return INSTANCE.internalEventSender(managingObject, properties);
    }


    private void initConnections(Properties properties)
    {
        if (properties.containsKey("bootstrap.servers"))
        {
            validateKafkaConnection(properties);
        }
    }

    private EventSender internalEventSender(Object managingObject, Properties properties)
    {
        return INSTANCE.messageSenderMap.computeIfAbsent(managingObject,
                key ->  createEventSender(EventSenderPool.class, properties));

    }


    private EventSenderPool()
    {
        JexxaContext.registerCleanupHandler(messageSenderMap::clear);
        JexxaContext.registerValidationHandler(this::initConnections);
    }
}

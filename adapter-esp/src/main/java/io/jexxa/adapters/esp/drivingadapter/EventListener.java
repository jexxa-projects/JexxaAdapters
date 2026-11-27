package io.jexxa.adapters.esp.drivingadapter;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface EventListener {
    void onEvent(ConsumerRecord<?, ?> onEvent);
    Class<?> keyType();
    Class<?> valueType();
    String topic();

    default String groupID()
    {
        return getClass().getSimpleName();
    }

}

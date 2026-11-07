package io.jexxa.adapters.esp.drivingadapter;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;

public abstract class TypedEventListener<K, V> implements EventListener {

    private final Class<K> keyType;
    private final Class<V> valueType;

    protected TypedEventListener(Class<K> keyType, Class<V> valueType)
    {
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @Override
    public void onEvent(ConsumerRecord<?, ?> onEvent) {
        ConsumerRecord<K, V> consumerRecord = (ConsumerRecord<K,V>)onEvent;
        onEvent(consumerRecord.key(), consumerRecord.value(), consumerRecord.headers());
        
    }

    @Override
    public Class<?> keyType()
    {
        return keyType;
    }

    @Override
    public Class<?> valueType() {
        return valueType;
    }
    protected abstract void onEvent(V value);

    protected void onEvent(K ignoredKey, V value)
    {
        onEvent(value);
    }

    protected void onEvent(K key, V value, Headers ignoredHeaders)
    {
        onEvent(key, value);
    }

}

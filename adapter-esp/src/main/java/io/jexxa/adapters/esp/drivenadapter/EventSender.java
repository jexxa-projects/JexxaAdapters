package io.jexxa.adapters.esp.drivenadapter;



import io.jexxa.adapters.core.facade.utils.annotation.CheckReturnValue;

import java.util.Properties;

public abstract class EventSender {

    @CheckReturnValue
    public <K, V> EventStreamBuilder<K,V> send(V eventData){
        return new EventStreamBuilder<>(eventData, this);
    }

    @CheckReturnValue
    public <K, V> EventStreamBuilder<K,V> send(K key, V eventData){
        return new EventStreamBuilder<>(key, eventData, this);
    }

    protected <K, V> void sendAsJSON(K key, V eventData, String topic, Long timestamp)
    {
        sendAsJSON(key, eventData, topic,timestamp, new Properties());
    }

    protected <K, V> void sendAsAVRO(K key, V eventData, String topic, Long timestamp)
    {
        sendAsAVRO(key, eventData, topic, timestamp, new Properties());
    }

    protected <K, V> void sendAsText(K key, V eventData, String topic, Long timestamp)
    {
        sendAsText(key, eventData, topic, timestamp, new Properties());
    }

    protected abstract <K, V> void sendAsJSON(K key, V eventData, String topic, Long timestamp, Properties headers);

    protected abstract <K, V> void sendAsAVRO(K key, V eventData, String topic, Long timestamp, Properties headers);

    protected abstract <K, V> void sendAsText(K key, V eventData, String topic, Long timestamp, Properties headers);

}

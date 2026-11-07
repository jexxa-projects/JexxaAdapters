package io.jexxa.adapters.esp.drivenadapter;


import io.jexxa.adapters.core.facade.utils.annotation.CheckReturnValue;

import java.time.Instant;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

public class EventStreamBuilder<K,V> {
    private final K key;
    private final V event;
    private final EventSender eventSender;
    private final Properties headers = new Properties();

    private Long timestamp = null;
    private String topic;

    protected EventStreamBuilder(V event, EventSender eventSender){
        this(null, event, eventSender);
    }

    protected EventStreamBuilder(K key, V event, EventSender eventSender){
        this.key = key;
        this.event = event;
        this.eventSender = eventSender;
    }

    @CheckReturnValue
    public EventStreamBuilder<K,V> withTimestamp(Instant timestamp){
        this.timestamp = timestamp.toEpochMilli();
        return this;
    }

    @CheckReturnValue
    public EventStreamBuilder<K,V> toTopic(String topic){
        this.topic = requireNonNull(topic);
        return this;
    }

    @CheckReturnValue
    public EventStreamBuilder<K,V> addHeader(String key, String value)
    {
        headers.put(key, value);

        return this;
    }

    public void asJSON(){
        eventSender.sendAsJSON(key,
                event,
                requireNonNull(topic),
                timestamp, headers);
    }

    public void asAVRO(){
        eventSender.sendAsAVRO(key,
                event,
                requireNonNull(topic),
                timestamp, headers);
    }

    public void asText(){
        eventSender.sendAsText(key,
                event,
                requireNonNull(topic),
                timestamp, headers);
    }
}

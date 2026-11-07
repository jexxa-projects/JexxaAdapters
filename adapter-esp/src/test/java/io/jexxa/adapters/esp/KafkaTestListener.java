package io.jexxa.adapters.esp;


import io.jexxa.adapters.esp.drivingadapter.TypedEventListener;
import org.apache.kafka.common.header.Headers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KafkaTestListener<T> extends TypedEventListener<String, T>
{
    private final List<T> result = new ArrayList<>();
    private final List<Headers> headerList = new ArrayList<>();
    private final String groupID = UUID.randomUUID().toString(); // Since we use this listener in multiple tests, we need a unique groupid for each test
    private final String topic;
    public KafkaTestListener(Class<T> clazz, String topic) {
        super(String.class, clazz);
        this.topic = topic;
    }

    @Override
    protected void onEvent(T value) {
        result.add(value);
    }

    @Override
    public String topic() {
        return topic;
    }

    @Override
    public String groupID() {
        return groupID;
    }

    public List<T> getResult() {
        return result;
    }

    public List<Headers> getHeaders() {
        return headerList;
    }

    @Override
    protected void onEvent(String key, T value, Headers headers)
    {
        headerList.add(headers);
        onEvent(key, value);
    }
}
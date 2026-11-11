package io.jexxa.adapters.esp;


import io.jexxa.adapters.esp.drivenadapter.EventSender;
import io.jexxa.adapters.esp.drivenadapter.EventSenderFactory;
import io.jexxa.adapters.esp.drivenadapter.KafkaSender;
import io.jexxa.adapters.esp.drivingadapter.EventListener;
import io.jexxa.adapters.esp.drivingadapter.KafkaAdapter;
import io.jexxa.adapters.esp.drivingadapter.TypedEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import static io.jexxa.adapters.esp.drivenadapter.EventSenderFactory.createEventSender;


public class EventBinding implements AutoCloseable {
    private final Properties properties;
    private final List<KafkaAdapter> kafkaAdapters;
    private final EventSender eventSender;

    public EventBinding(Class<?> application, Properties properties)
    {
        EventSenderFactory.setDefaultEventSender(KafkaSender.class);
        this.properties = properties;
        this.kafkaAdapters = new ArrayList<>();
        this.eventSender = createEventSender(application, properties);
    }

    public EventSender getSender()
    {
        return eventSender;
    }

    public <K, V> EventListener getListener(String topic,
                                            Consumer<V> consumer,
                                            Class<K> keyType,
                                            Class<V> valueType)
    {
        var eventListener = new FunctionalConsumerListener<>(topic, consumer, keyType, valueType);
        var kafkaAdapter = new KafkaAdapter(properties);
        kafkaAdapter.register(eventListener);
        kafkaAdapter.start();
        kafkaAdapters.add(kafkaAdapter);

        return eventListener;
    }

    public void registerListener(EventListener eventListener)
    {
        var kafkaAdapter = new KafkaAdapter(properties);
        kafkaAdapter.register(eventListener);
        kafkaAdapter.start();
        kafkaAdapters.add(kafkaAdapter);
    }

    @Override
    public void close()  {
        kafkaAdapters.forEach(KafkaAdapter::stop);
        kafkaAdapters.clear();
    }

    private static class FunctionalConsumerListener<K,V> extends TypedEventListener<K,V> {
        private final Consumer<V> consumer;
        private final String topic;

        FunctionalConsumerListener(String topic,
                                   Consumer<V> consumer,
                                   Class<K> keyClass,
                                   Class<V> valueClass)
        {
            super(keyClass,valueClass);
            this.consumer = consumer;
            this.topic = topic;
        }
        @Override
        protected void onEvent(V value) {
            consumer.accept(value);
        }

        @Override
        public String topic() {
            return topic;
        }

    }
}

package io.jexxa.adapters.esp.drivingadapter;

import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer;

import io.jexxa.adapters.api.drivingadapter.IDrivingAdapter;
import io.jexxa.adapters.api.invocation.InvocationManager;
import io.jexxa.adapters.api.invocation.JexxaInvocationHandler;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.jexxa.adapters.core.facade.logger.SLF4jLogger.getLogger;
import static io.jexxa.adapters.core.facade.utils.properties.PropertiesPrefix.globalPrefix;
import static io.jexxa.adapters.core.facade.utils.properties.PropertiesUtils.removePrefixFromKeys;
import static java.util.Collections.singletonList;

public class KafkaAdapter implements IDrivingAdapter {
    private static final String JSON_KEY_TYPE = "json.key.type";
    private static final String JSON_VALUE_TYPE = "json.value.type";
    private final List<InnerKafkaStruct> eventListeners = new ArrayList<>();

    private final Properties properties;

    private ExecutorService executor;

    public KafkaAdapter(Properties properties) {
        this.properties = removePrefixFromKeys(properties, globalPrefix());
    }

    @Override
    public void register(Object port) {
        var eventListener = (EventListener)(port);
        var listenerProperties = createListenerProperties(properties, eventListener);
        var consumer = new KafkaConsumer<>(listenerProperties);
        var topic = eventListener.topic();

        consumer.subscribe(singletonList(topic));
        eventListeners.add(new InnerKafkaStruct(eventListener, consumer));

        getLogger(KafkaAdapter.class).info("Listening for messages on topic: {}", topic);
    }


    @Override
    public synchronized void start() {
        executor = Executors.newFixedThreadPool(eventListeners.size());
        eventListeners.forEach(element -> executor.submit( element::run ));
    }


    @Override
    public synchronized void stop() {
        eventListeners.forEach(InnerKafkaStruct::stop);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                getLogger(KafkaConsumer.class).warn("Force shutdown...");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(e);
        }
    }

    private Properties createListenerProperties(Properties properties, EventListener eventListener)
    {
        var listenerProperties = new Properties();
        listenerProperties.putAll(properties);

        // Add type information for JSON deserialization
        listenerProperties.putIfAbsent(JSON_KEY_TYPE, eventListener.keyType().getName());
        listenerProperties.putIfAbsent(JSON_VALUE_TYPE, eventListener.valueType().getName());
        listenerProperties.putIfAbsent(ConsumerConfig.GROUP_ID_CONFIG, eventListener.groupID());
        listenerProperties.putIfAbsent(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaJsonSchemaDeserializer.class.getName());
        listenerProperties.putIfAbsent(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaJsonSchemaDeserializer.class.getName());

        //Configure autocommit
        listenerProperties.putIfAbsent(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        listenerProperties.putIfAbsent(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");

        if(!Objects.equals(listenerProperties.getProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG), "false")) {
            getLogger(KafkaAdapter.class).warn("{} is not set to false -> This can cause message lost in case of an exception during processing the message", ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG);
        }

        if(!Objects.equals(listenerProperties.getProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG), "1")) {
            getLogger(KafkaAdapter.class).warn("{} is not set to 1 -> This can cause message lost in case of an exception during processing the message", ConsumerConfig.MAX_POLL_RECORDS_CONFIG);
            listenerProperties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
        }


        return listenerProperties;
    }


    private static class InnerKafkaStruct
    {
        private final KafkaConsumer<?, ?> kafkaConsumer;
        private final EventListener eventListener;
        private final JexxaInvocationHandler invocationHandler;
        private boolean isRunning = false;

        InnerKafkaStruct(EventListener eventListener, KafkaConsumer<?, ?> kafkaConsumer)
        {
            this.kafkaConsumer = kafkaConsumer;
            this.eventListener = eventListener;
            this.invocationHandler = InvocationManager.getInvocationHandler(eventListener);
        }

        public void run(){
            isRunning = true;
            try {
                while (true) {
                    synchronized (this) {
                        if (!isRunning) {
                            break;
                        }
                    }
                    ConsumerRecords<?, ?> records = kafkaConsumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<?, ?> consumerRecord : records) {
                        processRecord(consumerRecord);
                    }
                }
            } catch (WakeupException _) {
                if (isRunning ){
                    getLogger(KafkaAdapter.class).error("Received wakeup exception while not listening");
                }
            }

            kafkaConsumer.close();
        }

        private void processRecord(ConsumerRecord<?,?> consumerRecord) {
            try {
                invocationHandler.invoke(eventListener, eventListener::onEvent, consumerRecord);
                kafkaConsumer.commitSync(Collections.singletonMap(
                        new TopicPartition(consumerRecord.topic(), consumerRecord.partition()),
                        new OffsetAndMetadata(consumerRecord.offset() + 1)
                ));
            } catch (Exception e) {
                getLogger(KafkaAdapter.class).warn("Could not process record. Reason {} -> Do not commit record and seek to kafka-offset {} for retry.", e.getMessage(), consumerRecord.offset());
                kafkaConsumer.pause(kafkaConsumer.assignment());
                kafkaConsumer.seek(
                        new TopicPartition(consumerRecord.topic(), consumerRecord.partition()),
                        consumerRecord.offset());
                try {
                    Thread.sleep(500 );
                } catch (InterruptedException _) {
                    Thread.currentThread().interrupt();
                }
                kafkaConsumer.resume(kafkaConsumer.assignment());
            }
        }

        public void stop()
        {
            synchronized (this) {
                isRunning = false;
                kafkaConsumer.wakeup();
            }

        }
    }

}

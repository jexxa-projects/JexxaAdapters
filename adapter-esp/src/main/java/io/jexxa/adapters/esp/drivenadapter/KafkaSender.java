package io.jexxa.adapters.esp.drivenadapter;

import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;

import java.util.Properties;

import static io.jexxa.adapters.core.facade.utils.properties.PropertiesPrefix.globalPrefix;
import static io.jexxa.adapters.core.facade.utils.properties.PropertiesUtils.removePrefixFromKeys;
import static io.jexxa.adapters.esp.drivenadapter.KafkaPool.kafkaProducer;

public class KafkaSender extends EventSender {
    private final Properties properties;

    public static KafkaSender kafkaSender(Properties filterProperties)
    {
        return new KafkaSender(filterProperties);
    }

    public KafkaSender(Properties properties) {
        this.properties = removePrefixFromKeys(properties, globalPrefix());
    }

    @Override
    protected <K, V> void sendAsJSON(K key, V eventData, String topic, Long timestamp, Properties headers) {
        setSerializerIfAbsent(KafkaJsonSchemaSerializer.class);
        internalSend(key, eventData, topic, timestamp, headers);
    }

    @Override
    protected <K, V> void sendAsAVRO(K key, V eventData, String topic, Long timestamp, Properties headers) {
        //Not implemented yet
    }

    @Override
    protected <K, V> void sendAsText(K key, V eventData, String topic, Long timestamp, Properties headers) {
        setSerializerIfAbsent(GenericStringSerializer.class);
        internalSend(key, eventData, topic, timestamp, headers);
    }

    private <T> void setSerializerIfAbsent(Class<T> clazz)
    {
        this.properties.setProperty(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                properties.getProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, clazz.getName())
        );

        this.properties.setProperty(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                properties.getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, clazz.getName())
        );
    }

    private void internalSend(Object key, Object eventData, String topic, Long timestamp, Properties headers)
    {
        var producer = kafkaProducer(properties);
        var producerRecord = new ProducerRecord<>(topic, null, timestamp, key, eventData);

        headers.forEach( (hKey, hValue) -> producerRecord.headers().add(
                new RecordHeader((String)hKey, ((String)hValue).getBytes()))
        );

        producer.send(producerRecord);
    }

}

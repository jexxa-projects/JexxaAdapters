package io.jexxa.adapters.esp;

import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer;
import io.jexxa.adapters.core.facade.logger.SLF4jLogger;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static io.jexxa.adapters.core.facade.utils.properties.PropertiesPrefix.globalPrefix;


@SuppressWarnings("unused")
public class EventStreamingPlatform {
    private final KafkaContainer kafkaBroker;//Conflunce-Kafka funktioniert mit SchemaRegistry nicht
    private final GenericContainer<?> schemaRegistry;
    private final Properties kafkaProperties;
    private static final String CONSUMER_GROUP_ID = "DigiSpineJSON";
    private static final String OFFSET_RESET = "earliest";


    public EventStreamingPlatform()
    {
        Network network = Network.newNetwork();
        kafkaBroker = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:8.1.0"))
                .withNetwork(network)
                .withNetworkAliases("kafka")
                .withKraft();
        kafkaBroker.start();

        schemaRegistry = new GenericContainer<>(
                DockerImageName.parse("confluentinc/cp-schema-registry:8.1.0"))
                .withNetwork(network)
                .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:9092")
                .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
                .withExposedPorts(Integer.valueOf(8081))
                .waitingFor(Wait.forHttp("/subjects"));
        schemaRegistry.start();

        var schemaRegistryUrl = "http://" + schemaRegistry.getHost() + ":" + schemaRegistry.getMappedPort(8081);
        kafkaProperties = new Properties();
        kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker.getBootstrapServers());
        kafkaProperties.put("schema.registry.url", schemaRegistryUrl);
    }

    /**
     * Creates a new Properties object containing all base configuration for kafka and schema registry
     */
    public Properties kafkaProperties()
    {
        Properties properties = new Properties();
        var schemaRegistryUrl = "http://" + schemaRegistry.getHost() + ":" + schemaRegistry.getMappedPort(8081);

        properties.put(globalPrefix() + ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker.getBootstrapServers());
        properties.put(globalPrefix() + "schema.registry.url", schemaRegistryUrl);
        return properties;
    }

    public void reset()
    {
        deleteTopics();
    }

    public void stop()
    {
        schemaRegistry.stop();
        kafkaBroker.stop();
        schemaRegistry.close();
        kafkaBroker.close();
    }

    @SuppressWarnings("unused")
    public void createTopic(String topic){
        try (AdminClient admin = AdminClient.create(Collections.singletonMap(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker.getBootstrapServers()))) {
            if( ! admin.listTopics().names().get().contains(topic ) )
            {
                admin.createTopics(Collections.singletonList(new NewTopic(topic, 1, (short) 1))).all().get();
            } else {
                SLF4jLogger.getLogger(EventStreamingPlatform.class).warn("Topic {} already exist", topic);
            }
        } catch (ExecutionException  e) {
            throw new IllegalArgumentException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException(e);
        }
    }


    public boolean topicExist(String topic){
        try (AdminClient admin = AdminClient.create(Collections.singletonMap(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker.getBootstrapServers()))) {
            return admin.listTopics().names().get().contains(topic ) ;
        } catch (ExecutionException  e) {
            throw new IllegalArgumentException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException(e);
        }
    }

    @SuppressWarnings("unused")
    public void deleteTopic(String topic){
        try (AdminClient admin = AdminClient.create(Collections.singletonMap(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker.getBootstrapServers()))) {
            if( admin.listTopics().names().get().contains(topic ) )
            {
                admin.deleteTopics(Collections.singletonList(topic));
            } else {
                SLF4jLogger.getLogger(EventStreamingPlatform.class).warn("Topic {} does not exist", topic);
            }
        }catch (ExecutionException  e) {
            throw new IllegalArgumentException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException(e);
        }
    }

    public void deleteTopics(){
        try (AdminClient admin = AdminClient.create(Collections.singletonMap(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker.getBootstrapServers()))) {
            //Do not delete system topics starting with the leading '_'
            var topics = admin.listTopics().names().get().stream().filter( element -> !element.startsWith("_")).toList();
            admin.deleteTopics(topics);
        } catch (ExecutionException  e) {
            throw new IllegalArgumentException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException(e);
        }
    }

    public List<String> messages(String topic, Duration duration)
    {
        return receiveGenericMessage(consumerPropertiesText(),topic, duration);
    }

    public <T> List<T> messagesFromJSON(String topic, Duration duration, Class<T> valueType)
    {
        return receiveGenericMessage(consumerPropertiesJSON(valueType),topic, duration);
    }

    public <K,V> Optional<ConsumerRecord<K,V>> latestRecord(String topic, Duration duration, Class<K> keyType, Class<V> valueType)
    {
        List<ConsumerRecord<K, V>> result = receiveGenericRecord(consumerPropertiesJSON(keyType, valueType),topic,duration);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.getFirst());
    }

    public Optional<String> latestMessage(String topic, Duration duration)
    {
        var result = messages(topic, duration);
        if (result.isEmpty())
        {
            return Optional.empty();
        }
        return Optional.of(result.getLast());
    }

    public <T> Optional<T> latestMessageFromJSON(String topic, Duration duration, Class<T> valueType)
    {
        var result = messagesFromJSON(topic, duration, valueType);
        if (result.isEmpty())
        {
            return Optional.empty();
        }
        return Optional.of(result.getLast());
    }

    private static <T> List<T> receiveGenericMessage(Properties consumerProps, String topic, Duration duration)
    {
        List<T> result = new ArrayList<>();

        try (KafkaConsumer<String, T> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(Collections.singletonList(topic));
            ConsumerRecords<String, T> records = consumer.poll(duration);
            records.forEach(element -> result.add(element.value()));
        }
        return result;
    }


    private static <K, V> List<ConsumerRecord<K,V>> receiveGenericRecord(Properties consumerProps, String topic, Duration duration)
    {
        List<ConsumerRecord<K,V>> result = new ArrayList<>();

        try (KafkaConsumer<K, V> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(Collections.singletonList(topic));
            ConsumerRecords<K, V> records = consumer.poll(duration);
            records.forEach(result::add);
        }
        return result;
    }

    private <T> Properties consumerPropertiesJSON(Class<T> clazz)
    {
        Properties consumerProperties = kafkaPropertiesWithoutPrefix();
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaJsonSchemaDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaJsonSchemaDeserializer.class.getName());
        consumerProperties.put("json.value.type", clazz.getName());
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OFFSET_RESET);

        return consumerProperties;
    }
    private <K,V> Properties consumerPropertiesJSON(Class<K> clazzKey, Class<V> clazzValue)
    {
        Properties consumerProperties = kafkaPropertiesWithoutPrefix();
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaJsonSchemaDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaJsonSchemaDeserializer.class.getName());
        consumerProperties.put("json.value.type", clazzValue.getName());
        consumerProperties.put("json.value.key", clazzKey.getName());
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OFFSET_RESET);

        return consumerProperties;
    }

    private Properties consumerPropertiesText()
    {
        Properties consumerProperties = kafkaPropertiesWithoutPrefix();
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OFFSET_RESET);

        return consumerProperties;
    }

    private Properties kafkaPropertiesWithoutPrefix()
    {
        Properties properties = new Properties();
        properties.putAll(kafkaProperties);
        return properties;
    }

}

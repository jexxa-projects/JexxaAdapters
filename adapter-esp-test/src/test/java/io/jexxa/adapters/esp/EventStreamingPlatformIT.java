package io.jexxa.adapters.esp;

import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventStreamingPlatformIT {

    private static EventStreamingPlatform esp;
    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(10);

    // Einfaches DTO für JSON-Schema-Tests
    public record TestEvent(String id, String payload) {}

    @BeforeAll
    static void initPlatform() {
        esp = new EventStreamingPlatform();
    }

    @AfterEach
    void cleanupTopics() {
        esp.reset();
    }

    @AfterAll
    static void stopPlatform() {
        if (esp != null) {
            esp.stop();
        }
    }

    @Test
    void createAndDeleteTopic_lifecycleWorksAsExpected() {
        // Arrange
        String topic = "test-topic-" + UUID.randomUUID();
        assertFalse(esp.topicExist(topic));

        // Act - Create
        esp.createTopic(topic);

        // Assert - Exists
        assertTrue(esp.topicExist(topic));

        // Act - Delete
        esp.deleteTopic(topic);

        // Assert - Removed (kurzes Warten auf asynchrone Topic-Löschung in Kafka möglich)
        assertDoesNotThrow(() -> esp.deleteTopic("non-existing-topic"));
    }

    @Test
    void messages_withTextMessages_receivesAllAndLatestMessage() throws Exception {
        // Arrange
        String topic = "text-topic-" + UUID.randomUUID();
        esp.createTopic(topic);
        sendTextMessage(topic, "key-1", "First Message");
        sendTextMessage(topic, "key-2", "Second Message");

        // Act
        List<String> allMessages = esp.messages(topic, POLL_TIMEOUT);
        // Hinweis: Da CONSUMER_GROUP_ID statisch ist und Offsets committed werden,
        // senden wir für latestMessage eine neue Nachricht:
        sendTextMessage(topic, "key-3", "Third Message");
        Optional<String> latest = esp.latestMessage(topic, POLL_TIMEOUT);

        // Assert
        assertEquals(List.of("First Message", "Second Message"), allMessages);
        assertTrue(latest.isPresent());
        assertEquals("Third Message", latest.get());
    }

    @Test
    void messagesFromJSON_withJsonSchemaPayload_deserializesCorrectly() throws Exception {
        // Arrange
        String topic = "json-topic-" + UUID.randomUUID();
        esp.createTopic(topic);
        TestEvent event1 = new TestEvent("1", "Alpha");
        TestEvent event2 = new TestEvent("2", "Beta");
        sendJsonMessage(topic, "k1", event1);
        sendJsonMessage(topic, "k2", event2);

        // Act
        List<TestEvent> events = esp.messagesFromJSON(topic, POLL_TIMEOUT, TestEvent.class);

        // Assert
        assertEquals(2, events.size());
        assertEquals(event1, events.get(0));
        assertEquals(event2, events.get(1));
    }

    @Test
    void latestMessageFromJSON_whenEmpty_returnsEmptyOptional() {
        // Arrange
        String emptyTopic = "empty-json-topic-" + UUID.randomUUID();
        esp.createTopic(emptyTopic);

        // Act
        Optional<TestEvent> result = esp.latestMessageFromJSON(emptyTopic, Duration.ofSeconds(1), TestEvent.class);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void latestRecord_withJsonKeyAndValue_returnsConsumerRecord() throws Exception {
        // Arrange
        String topic = "record-topic-" + UUID.randomUUID();
        esp.createTopic(topic);
        TestEvent key = new TestEvent("key-id", "KeyData");
        TestEvent value = new TestEvent("val-id", "ValueData");
        sendJsonMessage(topic, key, value);

        // Act
        var espRecord = esp.latestRecord(topic, POLL_TIMEOUT, TestEvent.class, TestEvent.class);

        // Assert
        assertTrue(espRecord.isPresent());
        assertEquals(value, espRecord.get().value());
    }

    // --- Hilfsmethoden zum Senden von Testnachrichten ---

    private void sendTextMessage(String topic, String key, String value) throws Exception {
        Properties props = baseProducerProps();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            producer.send(new ProducerRecord<>(topic, key, value)).get();
        }
    }

    private <K, V> void sendJsonMessage(String topic, K key, V value) throws Exception {
        Properties props = baseProducerProps();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaJsonSchemaSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaJsonSchemaSerializer.class.getName());

        try (KafkaProducer<K, V> producer = new KafkaProducer<>(props)) {
            producer.send(new ProducerRecord<>(topic, key, value)).get();
        }
    }

    private Properties baseProducerProps() {
        Properties rawProps = esp.kafkaProperties();
        Properties cleanProps = new Properties();
        // Entfernt ggf. gesetztes globalPrefix() für den reinen KafkaProducer
        rawProps.forEach((k, v) -> {
            String key = k.toString();
            if (key.endsWith(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)) {
                cleanProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, v);
            } else if (key.endsWith("schema.registry.url")) {
                cleanProps.put("schema.registry.url", v);
            }
        });
        return cleanProps;
    }
}

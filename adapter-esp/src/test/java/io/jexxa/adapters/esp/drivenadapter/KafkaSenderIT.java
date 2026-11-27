package io.jexxa.adapters.esp.drivenadapter;

import io.jexxa.adapters.esp.KafkaTestListener;
import io.jexxa.adapters.esp.drivingadapter.KafkaAdapter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;


import static io.jexxa.adapters.esp.KafkaUtilities.deleteTopics;
import static io.jexxa.adapters.esp.KafkaUtilities.kafkaProperties;
import static io.jexxa.adapters.esp.drivenadapter.KafkaSender.kafkaSender;
import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

class KafkaSenderIT {
    private static final String TEST_TEXT_TOPIC = "test-text-topic";
    private static final String TEST_JSON_TOPIC = "test-json-topic";

    @BeforeEach
    void initTests()
    {
        deleteTopics();
    }

    @Test
    void sendAsJSON() {
        //Arrange
        var expectedResult = new KafkaTestMessage(1, Instant.now(), "test message");
        var kafkaAdapter = new KafkaAdapter(kafkaProperties());
        var listener = new KafkaTestListener<>(KafkaTestMessage.class, TEST_JSON_TOPIC);
        kafkaAdapter.register(listener);
        kafkaAdapter.start();

        var objectUnderTest = kafkaSender( kafkaProperties());

        //Act
        objectUnderTest
                .send("test", expectedResult)
                .withTimestamp(now())
                .toTopic(TEST_JSON_TOPIC)
                .asJSON();

        await().atMost(Duration.ofSeconds(15)).until(() -> !listener.getResult().isEmpty());
        var result = listener.getResult();
        //Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(expectedResult, result.getFirst());
        kafkaAdapter.stop();
    }

    @Test
    void sendJSONWithHeader() {
        //Arrange
        var expectedResult = new KafkaTestMessage(1, Instant.now(), "test message");

        var kafkaAdapter = new KafkaAdapter(kafkaProperties());
        var listener = new KafkaTestListener<>(KafkaTestMessage.class, TEST_JSON_TOPIC);
        kafkaAdapter.register(listener);
        kafkaAdapter.start();

        var objectUnderTest = kafkaSender(kafkaProperties());

        //Act
        objectUnderTest
                .send("test", expectedResult)
                .withTimestamp(now())
                .addHeader("header", "value")
                .toTopic(TEST_JSON_TOPIC)
                .asJSON();

        await().atMost(Duration.ofSeconds(15)).until(() -> !listener.getResult().isEmpty());
        var result = listener.getResult();

        //Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(expectedResult, result.getFirst());
        assertEquals("value" ,new String(listener.getHeaders().getFirst().lastHeader("header").value(), StandardCharsets.UTF_8));
        kafkaAdapter.stop();

    }


    @Test
    void sendAsText() {
        // Arrange

        var properties = kafkaProperties();
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        var objectUnderTest = kafkaSender(properties);
        var kafkaAdapter = new KafkaAdapter(properties);
        var listener = new KafkaTestListener<>(String.class, TEST_TEXT_TOPIC);
        kafkaAdapter.register(listener);
        kafkaAdapter.start();

        var expectedResult = new KafkaTestMessage(1, Instant.now(), "test message");

        //Act
        objectUnderTest
                .send("test", expectedResult)
                .withTimestamp(now())
                .toTopic(TEST_TEXT_TOPIC)
                .asText();

        await().atMost(Duration.ofSeconds(15)).until(() -> !listener.getResult().isEmpty());

        //Assert
        assertFalse(listener.getResult().isEmpty());
        assertEquals(expectedResult.toString(), listener.getResult().getFirst());
        kafkaAdapter.stop();
    }


    public record KafkaTestMessage(int counter, Instant timestamp, String message) { }
}
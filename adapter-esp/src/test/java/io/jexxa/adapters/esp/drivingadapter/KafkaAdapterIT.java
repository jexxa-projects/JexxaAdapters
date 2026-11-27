package io.jexxa.adapters.esp.drivingadapter;

import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer;
import io.jexxa.adapters.core.facade.logger.SLF4jLogger;
import io.jexxa.adapters.esp.KafkaTestListener;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static io.jexxa.adapters.esp.KafkaUtilities.deleteTopics;
import static io.jexxa.adapters.esp.KafkaUtilities.kafkaProperties;
import static io.jexxa.adapters.esp.drivenadapter.KafkaSender.kafkaSender;
import static java.time.Instant.now;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

class KafkaAdapterIT {

    private static final String TEST_MESSAGE1_JSON_TOPIC = "test-message1-json-topic";
    private static final String TEST_MESSAGE2_JSON_TOPIC = "test-message2-json-topic";


    @BeforeEach
    void init() {
        deleteTopics();
    }

    @Test
    void receiveFromAdapterTest() {
        //Arrange
        var expectedResult = new KafkaFirstTestMessage(1, Instant.now(), "test message");

        Properties consumerProperties = kafkaProperties();
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaJsonSchemaDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaJsonSchemaDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        var objectUnderTest = new KafkaAdapter(consumerProperties);
        var listener = new KafkaTestListener<>(KafkaFirstTestMessage.class, TEST_MESSAGE1_JSON_TOPIC);
        objectUnderTest.register(listener);

        var sender = kafkaSender( kafkaProperties());
        //Act
        objectUnderTest.start();
        sender.send("test", expectedResult)
                .withTimestamp(now())
                .toTopic(TEST_MESSAGE1_JSON_TOPIC)
                .asJSON();

        //Assert/Await
        await().atMost(15, TimeUnit.SECONDS).until( () -> (!listener.getResult().isEmpty()));
        Assertions.assertEquals(expectedResult, listener.getResult().getFirst());

        objectUnderTest.stop();
    }

    @Test
    void retryMessageInCaseOfExceptionTest() {
        //Arrange
        var expectedResult = new KafkaFirstTestMessage(1, Instant.now(), "test message");

        Properties consumerProperties = kafkaProperties();
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaJsonSchemaDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaJsonSchemaDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        var objectUnderTest = new KafkaAdapter(consumerProperties);
        var listener = new KafkaExceptionListener<>(KafkaFirstTestMessage.class, TEST_MESSAGE1_JSON_TOPIC);
        objectUnderTest.register(listener);

        var sender = kafkaSender(kafkaProperties());
        //Act
        objectUnderTest.start();
        sender.send("test", expectedResult)
                .withTimestamp(now())
                .toTopic(TEST_MESSAGE1_JSON_TOPIC)
                .asJSON();

        //Assert/Await
        await().atMost(30, TimeUnit.SECONDS).until( () -> (listener.getResult().size() == 2));
        Assertions.assertEquals(expectedResult, listener.getResult().get(0));
        Assertions.assertEquals(expectedResult, listener.getResult().get(1));

        objectUnderTest.stop();
    }

    @Test
    void multipleTopicsTest() {
        //Arrange
        var expectedResult1 = new KafkaFirstTestMessage(1, Instant.now(), "test message");
        var expectedResult2 = new KafkaSecondTestMessage("Hello", Instant.now(), "test message");

        Properties consumerProperties = kafkaProperties();
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaJsonSchemaDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaJsonSchemaDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        var objectUnderTest = new KafkaAdapter(consumerProperties);
        var firstListener = new KafkaTestListener<>(KafkaFirstTestMessage.class, TEST_MESSAGE1_JSON_TOPIC);
        var secondListener = new KafkaTestListener<>(KafkaSecondTestMessage.class, TEST_MESSAGE2_JSON_TOPIC);

        objectUnderTest.register(firstListener);
        objectUnderTest.register(secondListener);

        //Act
        objectUnderTest.start();
        kafkaSender(kafkaProperties())
                .send("test", expectedResult1)
                .withTimestamp(now())
                .toTopic(TEST_MESSAGE1_JSON_TOPIC)
                .asJSON();

        kafkaSender(kafkaProperties())
                .send("test", expectedResult2)
                .withTimestamp(now())
                .toTopic(TEST_MESSAGE2_JSON_TOPIC)
                .asJSON();


        //Assert/Await
        await().atMost(15, TimeUnit.SECONDS).until( () -> (!firstListener.getResult().isEmpty()));
        await().atMost(15, TimeUnit.SECONDS).until( () -> (!secondListener.getResult().isEmpty()));

        Assertions.assertEquals(expectedResult1, firstListener.getResult().getFirst());
        Assertions.assertEquals(expectedResult2, secondListener.getResult().getFirst());

        objectUnderTest.stop();
    }



    record KafkaFirstTestMessage(int counter, Instant timestamp, String message) { }
    record KafkaSecondTestMessage(String message1, Instant timestamp, String message2) { }





    static class KafkaExceptionListener<T> extends KafkaTestListener<T> {
        KafkaExceptionListener(Class<T> clazz, String topic) {
            super(clazz, topic);
        }

        @Override
        protected void onEvent(T value) {
            super.onEvent(value);
            if (getResult().size() % 2 != 0)
            {
                SLF4jLogger.getLogger(KafkaExceptionListener.class).warn("KafkaExceptionListener: Simulate raising an exception during message processing");
                throw new IllegalStateException("KafkaExceptionListener: Simulate raising an exception during message processing");
            }
        }

    }
}
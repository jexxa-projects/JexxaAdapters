package io.jexxa.adapters.esp.drivenadapter;

import io.jexxa.adapters.esp.drivenadapter.logging.EventLogger;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

import static io.jexxa.adapters.esp.drivenadapter.EventProperties.eventStrategy;
import static org.junit.jupiter.api.Assertions.*;

class EventSenderFactoryTest {

    // --- Test-Implementierungen für die verschiedenen Instanziierungswege ---

    static class DummyAggregate {}

    // Basisklasse für alle Test-Stubs, passend zur abstrakten Klasse EventSender
    abstract static class BaseTestEventSender extends EventSender {
        @Override
        protected <K, V> void sendAsJSON(K key, V eventData, String topic, Long timestamp, Properties headers) {
            // No-Op für Tests
        }

        @Override
        protected <K, V> void sendAsAVRO(K key, V eventData, String topic, Long timestamp, Properties headers) {
            // No-Op für Tests
        }

        @Override
        protected <K, V> void sendAsText(K key, V eventData, String topic, Long timestamp, Properties headers) {
            // No-Op für Tests
        }
    }

    // 1. Konstruktor mit Properties
    public static class ConstructorWithPropertiesSender extends BaseTestEventSender {
        public ConstructorWithPropertiesSender(Properties ignore) {}
    }

    // 2. Statische Factory-Methode mit Properties
    public static class FactoryMethodWithPropertiesSender extends BaseTestEventSender {
        private FactoryMethodWithPropertiesSender() {}
        public static EventSender create(Properties ignore) {
            return new FactoryMethodWithPropertiesSender();
        }
    }

    // 3. Default-Konstruktor (ohne Parameter)
    public static class DefaultConstructorSender extends BaseTestEventSender {
        public DefaultConstructorSender() {/* Test purpose */}
    }

    // 4. Statische Factory-Methode ohne Parameter
    public static class FactoryMethodWithoutPropertiesSender extends BaseTestEventSender {
        private FactoryMethodWithoutPropertiesSender() {}
        public static EventSender create() {
            return new FactoryMethodWithoutPropertiesSender();
        }
    }

    // 5. Ungültiger Sender (weder passender Konstruktor noch Factory-Methode)
    public static class UninstantiableSender extends BaseTestEventSender {
        private UninstantiableSender() {}
    }

    @BeforeEach
    @AfterEach
    void resetStaticFactoryState() throws Exception {
        Field strategyMapField = EventSenderFactory.class.getDeclaredField("STRATEGY_MAP");
        strategyMapField.setAccessible(true);
        ((Map<?, ?>) strategyMapField.get(null)).clear();

        Field defaultSenderField = EventSenderFactory.class.getDeclaredField("defaultEventSender");
        defaultSenderField.setAccessible(true);
        defaultSenderField.set(null, null);
    }

    @Test
    void createEventSender_withoutConfiguration_fallsBackToEventLogger() {
        // Arrange
        Properties properties = new Properties();

        // Act
        EventSender result = EventSenderFactory.createEventSender(DummyAggregate.class, properties);

        // Assert
        assertInstanceOf(EventLogger.class, result);
        assertEquals(EventLogger.class, EventSenderFactory.getDefaultDefaultSender(properties));
    }

    @Test
    void createEventSender_withAggregateSpecificStrategy_prioritizesSpecificStrategy() {
        // Arrange
        Properties properties = new Properties();
        properties.setProperty(eventStrategy(), DefaultConstructorSender.class.getName());
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        EventSenderFactory.setDefaultEventSender(FactoryMethodWithoutPropertiesSender.class);
        EventSenderFactory.setEventSender(ConstructorWithPropertiesSender.class, DummyAggregate.class);

        // Act
        EventSender result = EventSenderFactory.createEventSender(DummyAggregate.class, properties);

        // Assert
        assertInstanceOf(ConstructorWithPropertiesSender.class, result);
    }

    @Test
    void createEventSender_withDefaultSenderSet_prioritizesDefaultOverProperties() {
        // Arrange
        Properties properties = new Properties();
        properties.setProperty(eventStrategy(), DefaultConstructorSender.class.getName());
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        EventSenderFactory.setDefaultEventSender(FactoryMethodWithPropertiesSender.class);

        // Act
        EventSender result = EventSenderFactory.createEventSender(DummyAggregate.class, properties);

        // Assert
        assertInstanceOf(FactoryMethodWithPropertiesSender.class, result);
    }

    @Test
    void createEventSender_withExplicitProperty_usesConfiguredClass() {
        // Arrange
        Properties properties = new Properties();
        properties.setProperty(eventStrategy(), DefaultConstructorSender.class.getName());
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // Act
        EventSender result = EventSenderFactory.createEventSender(DummyAggregate.class, properties);

        // Assert
        assertInstanceOf(DefaultConstructorSender.class, result);
    }

    @Test
    void createEventSender_withInvalidClassNameInProperties_ignoresSettingAndUsesFallback() {
        // Arrange
        Properties properties = new Properties();
        properties.setProperty(eventStrategy(), "io.jexxa.does.not.ExistEventSender");

        // Act
        EventSender result = EventSenderFactory.createEventSender(DummyAggregate.class, properties);

        // Assert
        assertInstanceOf(EventLogger.class, result);
    }

    @Test
    void createEventSender_withFactoryMethodWithoutProperties_instantiatesViaFactory() {
        // Arrange
        Properties properties = new Properties();
        EventSenderFactory.setDefaultEventSender(FactoryMethodWithoutPropertiesSender.class);

        // Act
        EventSender result = EventSenderFactory.createEventSender(DummyAggregate.class, properties);

        // Assert
        assertInstanceOf(FactoryMethodWithoutPropertiesSender.class, result);
    }

    @Test
    void createEventSender_withUninstantiableSender_throwsNoSuchElementException() {
        // Arrange
        Properties properties = new Properties();
        EventSenderFactory.setDefaultEventSender(UninstantiableSender.class);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () ->
                EventSenderFactory.createEventSender(DummyAggregate.class, properties)
        );
    }

    @Test
    void setDefaultEventSender_withNull_throwsNullPointerException() {
        // Arrange
        Class<? extends EventSender> nullSender = null;

        // Act & Assert
        assertThrows(NullPointerException.class, () ->
                EventSenderFactory.setDefaultEventSender(nullSender)
        );
    }
}

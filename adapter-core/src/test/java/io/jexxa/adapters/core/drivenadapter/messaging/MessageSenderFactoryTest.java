package io.jexxa.adapters.core.drivenadapter.messaging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

import static io.jexxa.adapters.core.drivenadapter.messaging.MessageSenderProperties.messageSenderStrategy;
import static org.junit.jupiter.api.Assertions.*;

class MessageSenderFactoryTest {

    // --- Test-Implementierungen für die verschiedenen Instanziierungswege ---

    static class DummyAggregate {}

    // Basisklasse für alle Test-Stubs, um Boilerplate zu vermeiden
    abstract static class BaseTestMessageSender extends MessageSender {
        @Override
        protected void sendToQueue(String message, String destination, Properties messageProperties, MessageType messageType) {
            // No-Op für Tests
        }

        @Override
        protected void sendToTopic(String message, String destination, Properties messageProperties, MessageType messageType) {
            // No-Op für Tests
        }
    }

    // 1. Konstruktor mit Properties
    public static class ConstructorWithPropertiesSender extends BaseTestMessageSender {
        public ConstructorWithPropertiesSender(Properties ignore) {}
    }

    // 2. Statische Factory-Methode mit Properties
    public static class FactoryMethodWithPropertiesSender extends BaseTestMessageSender {
        private FactoryMethodWithPropertiesSender() {}
        public static MessageSender create(Properties ignore) {
            return new FactoryMethodWithPropertiesSender();
        }
    }

    // 3. Default-Konstruktor (ohne Parameter)
    public static class DefaultConstructorSender extends BaseTestMessageSender {
        public DefaultConstructorSender() {/*For testing purpuse*/ }
    }

    // 4. Statische Factory-Methode ohne Parameter
    public static class FactoryMethodWithoutPropertiesSender extends BaseTestMessageSender {
        private FactoryMethodWithoutPropertiesSender() {}
        public static MessageSender create() {
            return new FactoryMethodWithoutPropertiesSender();
        }
    }

    // 5. Ungültiger Sender (weder passender Konstruktor noch Factory-Methode)
    public static class UninstantiableSender extends BaseTestMessageSender {
        private UninstantiableSender() {}
    }

    @BeforeEach
    @AfterEach
    void resetStaticFactoryState() throws Exception {
        Field strategyMapField = MessageSenderFactory.class.getDeclaredField("STRATEGY_MAP");
        strategyMapField.setAccessible(true);
        ((Map<?, ?>) strategyMapField.get(null)).clear();

        Field defaultSenderField = MessageSenderFactory.class.getDeclaredField("defaultMessageSender");
        defaultSenderField.setAccessible(true);
        defaultSenderField.set(null, null);
    }

    @Test
    void createMessageSender_withoutConfiguration_fallsBackToMessageLogger() {
        // Arrange
        Properties properties = new Properties();

        // Act
        MessageSender result = MessageSenderFactory.createMessageSender(DummyAggregate.class, properties);

        // Assert
        assertInstanceOf(MessageLogger.class, result);
        assertEquals(MessageLogger.class, MessageSenderFactory.getDefaultMessageSender(properties));
    }

    @Test
    void createMessageSender_withAggregateSpecificStrategy_prioritizesSpecificStrategy() {
        // Arrange
        Properties properties = new Properties();
        properties.setProperty(messageSenderStrategy(), DefaultConstructorSender.class.getName());
        MessageSenderFactory.setDefaultMessageSender(FactoryMethodWithoutPropertiesSender.class);
        MessageSenderFactory.setMessageSender(ConstructorWithPropertiesSender.class, DummyAggregate.class);

        // Act
        MessageSender result = MessageSenderFactory.createMessageSender(DummyAggregate.class, properties);

        // Assert
        assertInstanceOf(ConstructorWithPropertiesSender.class, result);
    }

    @Test
    void createMessageSender_withDefaultSenderSet_prioritizesDefaultOverProperties() {
        // Arrange
        Properties properties = new Properties();
        properties.setProperty(messageSenderStrategy(), DefaultConstructorSender.class.getName());
        MessageSenderFactory.setDefaultMessageSender(FactoryMethodWithPropertiesSender.class);

        // Act
        MessageSender result = MessageSenderFactory.createMessageSender(DummyAggregate.class, properties);

        // Assert
        assertInstanceOf(FactoryMethodWithPropertiesSender.class, result);
    }

    @Test
    void createMessageSender_withExplicitProperty_usesConfiguredClass() {
        // Arrange
        Properties properties = new Properties();
        properties.setProperty(messageSenderStrategy(), DefaultConstructorSender.class.getName());

        // Act
        MessageSender result = MessageSenderFactory.createMessageSender(DummyAggregate.class, properties);

        // Assert
        assertInstanceOf(DefaultConstructorSender.class, result);
    }

    @Test
    void createMessageSender_withInvalidClassNameInProperties_ignoresSettingAndUsesFallback() {
        // Arrange
        Properties properties = new Properties();
        properties.setProperty(messageSenderStrategy(), "io.jexxa.does.not.ExistSender");

        // Act
        MessageSender result = MessageSenderFactory.createMessageSender(DummyAggregate.class, properties);

        // Assert
        assertInstanceOf(MessageLogger.class, result);
    }

    @Test
    void createMessageSender_withFactoryMethodWithoutProperties_instantiatesViaFactory() {
        // Arrange
        Properties properties = new Properties();
        MessageSenderFactory.setDefaultMessageSender(FactoryMethodWithoutPropertiesSender.class);

        // Act
        MessageSender result = MessageSenderFactory.createMessageSender(DummyAggregate.class, properties);

        // Assert
        assertInstanceOf(FactoryMethodWithoutPropertiesSender.class, result);
    }

    @Test
    void createMessageSender_withUninstantiableSender_throwsNoSuchElementException() {
        // Arrange
        Properties properties = new Properties();
        MessageSenderFactory.setDefaultMessageSender(UninstantiableSender.class);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () ->
                MessageSenderFactory.createMessageSender(DummyAggregate.class, properties)
        );
    }

    @Test
    void setDefaultMessageSender_withNull_throwsNullPointerException() {
        // Arrange
        Class<? extends MessageSender> nullSender = null;

        // Act & Assert
        assertThrows(NullPointerException.class, () ->
                MessageSenderFactory.setDefaultMessageSender(nullSender)
        );
    }
}

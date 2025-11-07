package io.jexxa.common.facade.utils.properties;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static io.jexxa.common.facade.jms.JMSProperties.jndiFactoryKey;
import static io.jexxa.common.facade.jms.JMSProperties.jndiPasswordKey;
import static io.jexxa.common.facade.jms.JMSProperties.jndiProviderUrlKey;
import static io.jexxa.common.facade.jms.JMSProperties.jndiUserKey;
import static io.jexxa.common.facade.utils.properties.PropertiesUtils.removePrefixFromKeys;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertiesUtilsTest {

    @Test
    void filterProperties() throws IOException {
        //Arrange
        var objectUnderTest = new Properties();
        objectUnderTest.load(getClass().getResourceAsStream("/application.properties"));

        //Act
        var result = PropertiesUtils.filterByPrefix(objectUnderTest, "test-jms-connection");

        //Assert
        assertEquals(4, result.size());
        assertTrue(result.containsKey(jndiFactoryKey()));
        assertTrue(result.containsKey(jndiProviderUrlKey()));
        assertTrue(result.containsKey(jndiUserKey()));
        assertTrue(result.containsKey(jndiPasswordKey()));
    }

    @Test
    void removePrefixFromProperties() throws IOException {
        //Arrange
        var properties = new Properties();
        properties.load(getClass().getResourceAsStream("/application.properties"));
        var objectUnderTest = PropertiesUtils.filterByPrefix(properties, "test-jms-connection");
        var expectedKeyStart = "naming.";

        //Act
        var result = removePrefixFromKeys(objectUnderTest, "java.");


        //Assert
        assertEquals(4, result.size());
        assertTrue(result.stringPropertyNames().stream()
                .allMatch(key -> key.startsWith(expectedKeyStart)));
    }
}
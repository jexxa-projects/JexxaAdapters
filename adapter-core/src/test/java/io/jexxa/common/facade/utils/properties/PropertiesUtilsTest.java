package io.jexxa.common.facade.utils.properties;

import io.jexxa.core.facade.utils.properties.PropertiesUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static io.jexxa.core.facade.utils.properties.PropertiesUtils.removePrefixFromKeys;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertiesUtilsTest {


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
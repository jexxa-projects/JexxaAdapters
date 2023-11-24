package io.jexxa.commons.facade.utils.properties;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static io.jexxa.commons.facade.jms.JMSProperties.JNDI_FACTORY_KEY;
import static io.jexxa.commons.facade.jms.JMSProperties.JNDI_PASSWORD_KEY;
import static io.jexxa.commons.facade.jms.JMSProperties.JNDI_PROVIDER_URL_KEY;
import static io.jexxa.commons.facade.jms.JMSProperties.JNDI_USER_KEY;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertiesUtilsTest {

    @Test
    void getProperties() throws IOException {
        //Arrange
        var objectUnderTest = new Properties();
        objectUnderTest.load(getClass().getResourceAsStream("/application.properties"));

        //Act
        var result = PropertiesUtils.getSubset(objectUnderTest, "test-jms-connection");

        //Assert
        assertTrue(result.containsKey(JNDI_FACTORY_KEY));
        assertTrue(result.containsKey(JNDI_PROVIDER_URL_KEY));
        assertTrue(result.containsKey(JNDI_USER_KEY));
        assertTrue(result.containsKey(JNDI_PASSWORD_KEY));
    }
}
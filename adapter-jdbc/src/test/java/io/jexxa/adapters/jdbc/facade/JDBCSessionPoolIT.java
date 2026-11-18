package io.jexxa.adapters.jdbc.facade;

import io.jexxa.adapters.api.ConfigurationFailedException;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static io.jexxa.adapters.core.drivenadapter.persistence.PersistenceProperties.repositoryStrategy;
import static io.jexxa.adapters.jdbc.facade.JDBCProperties.*;
import static org.junit.jupiter.api.Assertions.*;


record JDBCSessionPoolIT() {

    private static Properties validH2Properties() {
        var properties = new Properties();
        properties.setProperty(jdbcUrl(), "jdbc:h2:mem:jexxa_session_pool_test;DB_CLOSE_DELAY=-1");
        properties.setProperty(jdbcDriver(), "org.h2.Driver");
        properties.setProperty(jdbcUsername(), "sa");
        properties.setProperty(jdbcPassword(), "");
        return properties;
    }

    @Test
    void jdbcSessionShouldCreateNewSession() {
        var properties = validH2Properties();
        var parent = new Object();

        JDBCSession session = JDBCSessionPool.jdbcSession(properties, parent);
        assertNotNull(session);
    }

    @Test
    void shouldDoNothing_whenRepositoryStrategyIsPresent() {
        var properties = new Properties();
        properties.setProperty(repositoryStrategy(), "CUSTOM_STRATEGY");

        // Absichtlich ungültige URL: Würde initJDBCSessions nicht vorher abbrechen, flöge hier eine Exception
        properties.setProperty(jdbcUrl(), "jdbc:invalid:driver:url");

        assertDoesNotThrow(() -> JDBCSessionPool.INSTANCE.initJDBCSessions(properties));
    }

    @Test
    void shouldInitConnection_whenJdbcUrlIsPresentAndNoStrategy() {
        var properties = validH2Properties();

        assertDoesNotThrow(() -> JDBCSessionPool.INSTANCE.initJDBCSessions(properties));
    }

    @Test
    void shouldDoNothing_whenNeitherStrategyNorJdbcUrlIsPresent() {
        var emptyProperties = new Properties();

        assertDoesNotThrow(() -> JDBCSessionPool.INSTANCE.initJDBCSessions(emptyProperties));
    }

    @Test
    void shouldThrowConfigurationFailedException_whenConnectionFails() {
        JDBCConnectionPool.reset();
        var invalidUrl = "jdbc:unknown_driver:invalid_url";
        var properties = new Properties();
        properties.setProperty(jdbcUrl(), invalidUrl);

        var exception = assertThrows(
                ConfigurationFailedException.class,
                () -> JDBCSessionPool.INSTANCE.initJDBCSessions(properties)
        );

        assertAll(
                () -> assertTrue(exception.getMessage().contains(invalidUrl), "Fehlermeldung muss die URL enthalten"),
                () -> assertTrue(exception.getMessage().contains("Reason:"), "Fehlermeldung muss den Grund enthalten"),
                () -> assertNotNull(exception.getCause(), "Ursprüngliche RuntimeException muss als Cause gesetzt sein")
        );
    }
}

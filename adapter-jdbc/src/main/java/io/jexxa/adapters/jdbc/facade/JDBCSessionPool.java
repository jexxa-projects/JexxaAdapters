package io.jexxa.adapters.jdbc.facade;

import io.jexxa.adapters.api.ConfigurationFailedException;
import io.jexxa.adapters.api.JexxaContext;

import java.util.Properties;

import static io.jexxa.adapters.core.drivenadapter.persistence.PersistenceProperties.repositoryStrategy;
import static io.jexxa.adapters.jdbc.facade.JDBCConnectionPool.getJDBCConnection;
import static io.jexxa.adapters.jdbc.facade.JDBCProperties.jdbcUrl;

public class JDBCSessionPool {
    public static final JDBCSessionPool INSTANCE = new JDBCSessionPool();

    public static JDBCSession jdbcSession(Properties properties, Object parent) {
        return new JDBCSession(getJDBCConnection(properties, parent));
    }

    public void initJDBCSessions(Properties properties)
    {
        if (properties.containsKey(repositoryStrategy()))
        {
            return;
        }
        try {
            if (properties.containsKey(jdbcUrl())) {
                getJDBCConnection(properties, INSTANCE);
            }
        } catch ( RuntimeException e) {
            throw new ConfigurationFailedException("Could not init JDBC connection for filter properties " + properties.getProperty(jdbcUrl())
                    + ". Reason: " + e.getMessage(), e );
        }
    }

    private JDBCSessionPool()
    {
        JexxaContext.registerValidationHandler(this::initJDBCSessions);
    }

}

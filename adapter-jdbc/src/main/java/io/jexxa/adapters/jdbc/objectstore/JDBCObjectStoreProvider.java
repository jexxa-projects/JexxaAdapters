package io.jexxa.adapters.jdbc.objectstore;

import io.jexxa.adapters.core.drivenadapter.persistence.ObjectStoreProvider;
import io.jexxa.adapters.jdbc.facade.JDBCProperties;

import java.util.Optional;
import java.util.Properties;

public class JDBCObjectStoreProvider implements ObjectStoreProvider
{
    @Override
    public Optional<Class<?>> getObjectStoreType(Properties properties) {
        if (properties.containsKey(JDBCProperties.jdbcDriver()))
        {
            return Optional.of(JDBCObjectStore.class);
        }

        return Optional.empty();    }
}
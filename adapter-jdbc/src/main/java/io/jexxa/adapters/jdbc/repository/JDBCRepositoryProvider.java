package io.jexxa.adapters.jdbc.repository;

import io.jexxa.adapters.core.drivenadapter.persistence.RepositoryProvider;
import io.jexxa.adapters.jdbc.facade.JDBCProperties;

import java.util.Optional;
import java.util.Properties;

public class JDBCRepositoryProvider implements RepositoryProvider
{
    @Override
    public Optional<Class<?>> getRepositoryType(Properties properties)
    {
        if (properties.containsKey(JDBCProperties.jdbcDriver()))
        {
            return Optional.of(JDBCKeyValueRepository.class);
        }

        return Optional.empty();
    }
}
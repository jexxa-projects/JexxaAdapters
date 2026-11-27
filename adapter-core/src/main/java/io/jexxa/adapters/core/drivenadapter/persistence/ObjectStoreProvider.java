package io.jexxa.adapters.core.drivenadapter.persistence;

import java.util.Optional;
import java.util.Properties;

public interface ObjectStoreProvider
{
    Optional<Class<?>> getObjectStoreType(Properties properties);
}
package io.jexxa.adapters.s3.objectstore;

import io.jexxa.adapters.core.drivenadapter.persistence.ObjectStoreProvider;
import io.jexxa.adapters.s3.facade.S3Properties;

import java.util.Optional;
import java.util.Properties;

public class S3ObjectStoreProvider implements ObjectStoreProvider
{
    @Override
    public Optional<Class<?>> getObjectStoreType(Properties properties) {
        if (properties.containsKey(S3Properties.s3Bucket()))
        {
            return Optional.of(S3ObjectStore.class);
        }

        return Optional.empty();
    }
}
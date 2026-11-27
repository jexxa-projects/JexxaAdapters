package io.jexxa.adapters.s3.repository;


import io.jexxa.adapters.core.drivenadapter.persistence.RepositoryProvider;
import io.jexxa.adapters.s3.facade.S3Properties;

import java.util.Optional;
import java.util.Properties;

public class S3RepositoryProvider implements RepositoryProvider
{
    @Override
    public Optional<Class<?>> getRepositoryType(Properties properties)
    {
        if (properties.containsKey(S3Properties.s3Bucket()))
        {
            return Optional.of(S3KeyValueRepository.class);
        }

        return Optional.empty();
    }
}
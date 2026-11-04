package io.jexxa.common.drivenadapter.persistence.objectstore;

import io.jexxa.common.drivenadapter.persistence.RepositoryConfig;

import java.util.Properties;
import java.util.stream.Stream;

public final class ObjectStoreTestDatabase
{
    public static final String REPOSITORY_CONFIG = "io.jexxa.common.drivenadapter.persistence.objectstore.ObjectStoreTestDatabase#repositoryConfig";

    @SuppressWarnings("unused")
    public static Stream<Properties> repositoryConfig() {
        return RepositoryConfig.objectStoreConfig("objectstore");
    }

    private ObjectStoreTestDatabase()
    {
        //private constructor
    }
}

package io.jexxa.common.drivenadapter.persistence;



import java.util.Properties;
import java.util.stream.Stream;

import static io.jexxa.core.facade.s3.S3Properties.s3AccessKey;
import static io.jexxa.core.facade.s3.S3Properties.s3Bucket;
import static io.jexxa.core.facade.s3.S3Properties.s3Endpoint;
import static io.jexxa.core.facade.s3.S3Properties.s3SecretKey;


public class RepositoryConfig {

    @SuppressWarnings("unused")
    public static Stream<Properties> objectStoreConfig(String schemaName) {
        return Stream.of(
                imdbRepositoryConfig()
        );
    }

    @SuppressWarnings("unused") // Used in ParameterizedTest
    public static Stream<Properties> repositoryConfig(String schemaName) {
        return Stream.of(
                imdbRepositoryConfig()
        );
    }

    public static Properties s3RepositoryConfig() {
        var s3Properties = new Properties();
        s3Properties.put(s3Endpoint(), "http://localhost:8110");
        s3Properties.put(s3Bucket(), "jexxa-adapters-test");
        s3Properties.put(s3SecretKey(), "minioadmin");
        s3Properties.put(s3AccessKey(), "minioadmin");
        return s3Properties;
    }

    public static Properties s3CachedRepositoryConfig() {
        var s3Properties = new Properties();
        s3Properties.put("repository.strategy", "io.jexxa.common.drivenadapter.persistence.repository.s3.S3CachedKeyValueRepository");
        s3Properties.put(s3Endpoint(), "http://localhost:8110");
        s3Properties.put(s3Bucket(), "jexxa-adapters-test");
        s3Properties.put(s3SecretKey(), "minioadmin");
        s3Properties.put(s3AccessKey(), "minioadmin");
        return s3Properties;
    }


    public static Properties imdbRepositoryConfig()
    {
        return new Properties();
    }

}

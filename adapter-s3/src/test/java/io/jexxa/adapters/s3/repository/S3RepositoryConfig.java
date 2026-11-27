package io.jexxa.adapters.s3.repository;


import java.util.Properties;

import static io.jexxa.adapters.core.drivenadapter.persistence.PersistenceProperties.repositoryStrategy;
import static io.jexxa.adapters.s3.facade.S3Properties.*;


public class S3RepositoryConfig {


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
        s3Properties.put(repositoryStrategy(), S3CachedKeyValueRepository.class.getName());
        s3Properties.put(s3Endpoint(), "http://localhost:8110");
        s3Properties.put(s3Bucket(), "jexxa-adapters-test");
        s3Properties.put(s3SecretKey(), "minioadmin");
        s3Properties.put(s3AccessKey(), "minioadmin");
        return s3Properties;
    }

}

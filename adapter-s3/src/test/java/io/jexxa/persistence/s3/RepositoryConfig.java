package io.jexxa.persistence.s3;


import java.util.Properties;

import static io.jexxa.persistence.s3.facade.S3Properties.*;


public class RepositoryConfig {


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

}

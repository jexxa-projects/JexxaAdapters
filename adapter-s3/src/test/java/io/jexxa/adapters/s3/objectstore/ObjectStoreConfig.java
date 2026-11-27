package io.jexxa.adapters.s3.objectstore;


import java.util.Properties;

import static io.jexxa.adapters.s3.facade.S3Properties.s3AccessKey;
import static io.jexxa.adapters.s3.facade.S3Properties.s3Bucket;
import static io.jexxa.adapters.s3.facade.S3Properties.s3Endpoint;
import static io.jexxa.adapters.s3.facade.S3Properties.s3SecretKey;


public class ObjectStoreConfig {


    public static Properties s3ObjectStoreConfig() {
        var s3Properties = new Properties();
        s3Properties.put(s3Endpoint(), "http://localhost:8110");
        s3Properties.put(s3Bucket(), "jexxa-adapters-test");
        s3Properties.put(s3SecretKey(), "minioadmin");
        s3Properties.put(s3AccessKey(), "minioadmin");
        return s3Properties;
    }


}

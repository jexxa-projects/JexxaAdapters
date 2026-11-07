package io.jexxa.adapters.s3.objectstore;


import io.jexxa.adapters.core.drivenadapter.persistence.objectstore.INumericQueryIT;
import io.jexxa.adapters.s3.facade.S3Facade;

import java.util.Properties;
import java.util.stream.Stream;

import static io.jexxa.adapters.s3.repository.RepositoryConfig.s3CachedRepositoryConfig;
import static io.jexxa.adapters.s3.repository.RepositoryConfig.s3RepositoryConfig;
import static io.jexxa.adapters.s3.facade.S3Properties.s3Bucket;
import static io.jexxa.adapters.s3.facade.S3Properties.s3Endpoint;

class S3NumericQueryIT extends INumericQueryIT
{
    @Override
    protected void reset(Properties properties)
    {
        if (properties.containsKey(s3Endpoint())) {
            var s3Client = new S3Facade(properties);
            s3Client.removeObjects(s3Client.getAllS3Objects());
            s3Client.removeBucket(properties.getProperty(s3Bucket()));
        }
    }
    @SuppressWarnings("unused")
    static Stream<Properties> objectStoreConfig()
    {
        return Stream.of( s3RepositoryConfig(), s3CachedRepositoryConfig());
    }
}

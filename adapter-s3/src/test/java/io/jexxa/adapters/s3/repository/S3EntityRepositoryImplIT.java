package io.jexxa.adapters.s3.repository;

import io.jexxa.adapters.core.drivenadapter.persistence.repository.TestEntityRepositoryImplIT;
import io.jexxa.adapters.testapplication.TestConstants;
import io.jexxa.adapters.s3.facade.S3Facade;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Properties;
import java.util.stream.Stream;

import static io.jexxa.adapters.s3.RepositoryConfig.s3CachedRepositoryConfig;
import static io.jexxa.adapters.s3.RepositoryConfig.s3RepositoryConfig;
import static io.jexxa.adapters.s3.facade.S3Properties.s3Bucket;
import static io.jexxa.adapters.s3.facade.S3Properties.s3Endpoint;


@Execution(ExecutionMode.SAME_THREAD)
@Tag(TestConstants.INTEGRATION_TEST)
class S3EntityRepositoryImplIT extends TestEntityRepositoryImplIT
{
    protected void reset(Properties properties)
    {
        if (properties.containsKey(s3Endpoint())) {
            var s3Client = new S3Facade(properties);
            s3Client.removeObjects(s3Client.getAllS3Objects());
            s3Client.removeBucket(properties.getProperty(s3Bucket()));
        }
    }

    @SuppressWarnings("unused")
    static Stream<Properties> repositoryConfig()
    {
        return Stream.of( s3RepositoryConfig(), s3CachedRepositoryConfig());
    }

}
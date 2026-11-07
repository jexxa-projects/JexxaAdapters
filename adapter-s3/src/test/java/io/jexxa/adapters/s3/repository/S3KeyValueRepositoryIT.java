package io.jexxa.adapters.s3.repository;

import io.jexxa.adapters.testapplication.TestAggregate;
import org.junit.jupiter.api.Test;

import static io.jexxa.adapters.s3.repository.RepositoryConfig.s3RepositoryConfig;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class S3KeyValueRepositoryIT {

    @Test
    void initS3Bucket()
    {
        //Arrange
        assertDoesNotThrow(() -> new S3KeyValueRepository<>(
                TestAggregate.class,
                TestAggregate::getKey,
                s3RepositoryConfig()));

    }
}
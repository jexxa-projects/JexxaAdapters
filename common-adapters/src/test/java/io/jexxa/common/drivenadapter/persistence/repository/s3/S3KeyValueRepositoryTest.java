package io.jexxa.common.drivenadapter.persistence.repository.s3;

import io.jexxa.common.facade.testapplication.TestAggregate;
import org.junit.jupiter.api.Test;

import static io.jexxa.common.drivenadapter.persistence.RepositoryConfig.s3RepositoryConfig;
import static org.junit.jupiter.api.Assertions.*;

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
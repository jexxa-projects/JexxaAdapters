package io.jexxa.persistence.s3.repository;

import io.jexxa.common.testapplication.TestAggregate;
import org.junit.jupiter.api.Test;

import static io.jexxa.persistence.s3.RepositoryConfig.s3RepositoryConfig;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class S3KeyValueRepositoryIT  {

    @Test
    void initS3Bucket()
    {
        //Arrange
        assertDoesNotThrow(() -> new S3KeyValueRepository<>(
                TestAggregate.class,
                TestAggregate::getKey,
                s3RepositoryConfig()));

    }

          /*  if (properties.containsKey(jdbcUrl()))
        {
            try(JDBCConnection jdbcConnection = new JDBCConnection(properties))
            {
                jdbcConnection.tableCommand(TestObjectSchema.class)
                        .dropTableIfExists(TestObject.class)
                        .asIgnore();
            }
        }
*/

    /*    if (properties.containsKey(s3Endpoint())) {
            var s3Client = new S3Facade(properties);
            s3Client.removeObjects(s3Client.getAllS3Objects());
            s3Client.removeBucket(properties.getProperty(s3Bucket()));
        }
*/


}
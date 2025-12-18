package io.jexxa.common.facade.s3;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static io.jexxa.common.facade.s3.S3Properties.s3AccessKey;
import static io.jexxa.common.facade.s3.S3Properties.s3FileAccessKey;
import static io.jexxa.common.facade.s3.S3Properties.s3FileSecretKey;
import static io.jexxa.common.facade.s3.S3Properties.s3SecretKey;
import static io.jexxa.common.facade.utils.properties.PropertiesUtils.removePrefixFromKeys;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class S3ClientIT {

    @Test
    void initS3Bucket() throws IOException {

        //Arrange
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/application.properties"));

        var s3properties = removePrefixFromKeys(properties, "test-storage.");
        s3properties.remove(s3AccessKey());
        s3properties.remove(s3SecretKey());
        s3properties.put(s3FileAccessKey(), "src/test/resources/secrets/s3User");
        s3properties.put(s3FileSecretKey(), "src/test/resources/secrets/s3Password");

        //Act/Assert
        assertDoesNotThrow(() -> new S3Client(s3properties));
    }
}
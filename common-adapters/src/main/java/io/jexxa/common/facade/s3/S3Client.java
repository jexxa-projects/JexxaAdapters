package io.jexxa.common.facade.s3;

import io.jexxa.common.drivenadapter.persistence.repository.s3.S3KeyValueRepository;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static io.jexxa.common.facade.logger.SLF4jLogger.getLogger;
import static io.jexxa.common.facade.s3.S3Properties.s3AccessKey;
import static io.jexxa.common.facade.s3.S3Properties.s3Bucket;
import static io.jexxa.common.facade.s3.S3Properties.s3Endpoint;
import static io.jexxa.common.facade.s3.S3Properties.s3FileAccessKey;
import static io.jexxa.common.facade.s3.S3Properties.s3FileSecretKey;
import static io.jexxa.common.facade.s3.S3Properties.s3Region;
import static io.jexxa.common.facade.s3.S3Properties.s3SecretKey;
import static java.util.Objects.requireNonNull;

public class S3Client {
    private final MinioClient minioClient;
    private final Properties properties;
    public S3Client(Properties properties)
    {
        validateProperties(properties);
        this.properties = properties;
        minioClient = MinioClient.builder()
                .endpoint(properties.getProperty(s3Endpoint()))
                .credentials(
                        getFirstPropertyAvailable(properties, s3AccessKey(), s3FileAccessKey()),
                        getFirstPropertyAvailable(properties, s3SecretKey(), s3FileSecretKey()))
                .region(properties.getProperty(s3Region()))
                .build();
        initBucket();
    }

    private void initBucket()
    {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(properties.getProperty(s3Bucket())).build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(properties.getProperty(s3Bucket())).build()
                );
            }
        } catch (Exception e)
        {
            throw new IllegalArgumentException("Bucket could not be initialized! ", e);
        }
    }

    public void removeObject(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getProperty(s3Bucket()))
                            .object(objectName)
                            .build()
            );
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException _)
        {
            getLogger(S3KeyValueRepository.class).warn("Could not delete object {}", objectName);
        }
    }

    public void removeObjects(List<String> objectList) {
        Iterable<DeleteObject> objectsToDelete = objectList.stream()
                .map(DeleteObject::new)
                .toList();

        var result = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(properties.getProperty(s3Bucket()))
                        .objects(objectsToDelete)
                        .build()
        );
        try {
            for (Result<DeleteError> r : result) {
                DeleteError error = r.get();
                getLogger(S3KeyValueRepository.class).error("Error during delete of file: {} ", error.objectName());
            }
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Could not execute removeAll", e);
        }
    }

    public Optional<String> get(String objectName) {
        try (var stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(properties.getProperty(s3Bucket()))
                        .object(objectName)
                        .build()))
        {
            return Optional.of(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (MinioException | RuntimeException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            getLogger(S3KeyValueRepository.class).warn(e.getMessage());
        }

        return Optional.empty();
    }


    public void putObject(String objectName, InputStream objectStream, int objectSize) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getProperty(s3Bucket()))
                            .object(objectName)
                            .stream(objectStream, objectSize, -1)
                            .contentType("application/json")
                            .build()
            );
        } catch (IOException | MinioException | InvalidKeyException | NoSuchAlgorithmException _)
        {
            throw new IllegalArgumentException("Could not put object with id " + objectName );
        }
    }

    public boolean objectExist(String objectName)
    {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(properties.getProperty(s3Bucket()))
                            .object(objectName)
                            .build()
            );
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            }
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e)
        {
            getLogger(S3KeyValueRepository.class).warn(e.getMessage());
            return false;
        }

        return true;
    }

    public List<String> getAllS3Objects()
    {
       return getAllS3Objects("");
    }

    public List<String> getAllS3Objects(String prefix)
    {
        // Alle Objekte im Bucket auflisten
        var results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(properties.getProperty(s3Bucket()))
                        .prefix(prefix)
                        .recursive(true) // rekursiv über alle "Ordner" hinweg
                        .build()
        );

        try {
            List<String> objectNames = new ArrayList<>();

            for (Result<Item> result : results) {
                Item item = result.get();
                objectNames.add(item.objectName());
            }

            return objectNames;
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e)
        {
            getLogger(S3KeyValueRepository.class).warn(e.getMessage());
        }

        return Collections.emptyList();
    }

    public void removeBucket(String bucketName) {

        try {
            minioClient.removeBucket(
                    RemoveBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e)
        {
            throw new IllegalArgumentException("Could not delete bucket " + bucketName, e);
        }

    }
    private void validateProperties(Properties properties)
    {
        requireNonNull(properties.getProperty(s3Endpoint()));
        requireNonNull(properties.getProperty(s3Bucket()));
    }

    private String getFirstPropertyAvailable(Properties props, String... keys) {
        for (String key : keys) {
            String value = props.getProperty(key);
            if (value != null && !value.isEmpty()) return value;
        }
        throw new IllegalArgumentException("No valid properties found");
    }

}

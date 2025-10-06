package io.jexxa.common.drivenadapter.persistence.repository.s3;

import io.jexxa.common.drivenadapter.persistence.repository.IRepository;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

import static io.jexxa.common.facade.json.JSONManager.getJSONConverter;
import static io.jexxa.common.facade.logger.SLF4jLogger.getLogger;
import static io.jexxa.common.facade.s3.S3Properties.s3AccessKey;
import static io.jexxa.common.facade.s3.S3Properties.s3Bucket;
import static io.jexxa.common.facade.s3.S3Properties.s3Endpoint;
import static io.jexxa.common.facade.s3.S3Properties.s3FileAccessKey;
import static io.jexxa.common.facade.s3.S3Properties.s3FileSecretKey;
import static io.jexxa.common.facade.s3.S3Properties.s3Region;
import static io.jexxa.common.facade.s3.S3Properties.s3SecretKey;
import static java.util.Objects.requireNonNull;

public class S3KeyValueRepository<T,K> implements IRepository<T, K>
{
    private final Function<T,K> keyFunction;
    private final Class<T> aggregateClazz;
    private final MinioClient minioClient;
    private final Properties properties;


    public S3KeyValueRepository(Class<T> aggregateClazz, Function<T,K> keyFunction, Properties properties)
    {

        this.keyFunction = requireNonNull( keyFunction );
        this.aggregateClazz = requireNonNull(aggregateClazz);
        this.properties = properties;

        validateProperties(properties);

        minioClient = MinioClient.builder()
                .endpoint(properties.getProperty(s3Endpoint()))
                .credentials(
                        getFirstPropertyAvailable(properties, s3AccessKey(), s3FileAccessKey()),
                        getFirstPropertyAvailable(properties, s3SecretKey(), s3FileSecretKey()))
                .region(properties.getProperty(s3Region()))
                .build();
        initBucket();
    }

    @Override
    public void update(T aggregate) {
        // Datei schreiben (add oder update)
        var aggregateJSON =getJSONConverter().toJson(aggregate).getBytes(StandardCharsets.UTF_8);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(aggregateJSON)
        ) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getProperty(s3Bucket()))
                            .object(encodeFilename(keyFunction.apply(aggregate)))
                            .stream(inputStream, aggregateJSON.length, -1)
                            .contentType("application/json")
                            .build()
            );
        } catch (IOException | MinioException | InvalidKeyException | NoSuchAlgorithmException e)
        {
            throw new IllegalArgumentException("Could not add aggregate with id " + encodeFilename(keyFunction.apply(aggregate)) );
        }

    }

    @Override
    public void remove(K key) {
        if (!objectExist(key))
        {
            throw new IllegalArgumentException("Aggregate does not exist " + key);
        }
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getProperty(s3Bucket()))
                            .object(encodeFilename(key))
                            .build()
            );
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e)
        {
            getLogger(S3KeyValueRepository.class).warn("Could not delete object {}", key);
        }

    }

    @Override
    public void removeAll() {
        Iterable<DeleteObject> objectsToDelete = getAllS3Objects().stream()
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
                getLogger(S3KeyValueRepository.class).error("Error during delete of file: {} " , error.objectName());
            }
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e )
        {
            throw new IllegalArgumentException("Could not execute removeAll",e);
        }

    }

    @Override
    public void add(T aggregate) {
        if (objectExist(keyFunction.apply(aggregate))) {
            throw new IllegalArgumentException("Aggregate with key " + keyFunction.apply(aggregate).toString() + "already exists");
        }
        update(aggregate);
    }

    @Override
    public Optional<T> get(K key) {
        return get(encodeFilename(key));
    }

    @Override
    public List<T> get() {
        return getAllS3Objects()
                .stream()
                .map(this::get)
                .flatMap(Optional::stream )
                .toList();
    }

    private Optional<T> get(String objectName) {
        try (var stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(properties.getProperty(s3Bucket()))
                        .object(objectName)
                        .build()))
        {
            return Optional.of(
                    getJSONConverter().fromJson(new String(stream.readAllBytes(), StandardCharsets.UTF_8),
                            aggregateClazz)
            );
        } catch (MinioException | RuntimeException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            getLogger(S3KeyValueRepository.class).warn(e.getMessage());
        }

        return Optional.empty();
    }

    private void validateProperties(Properties properties)
    {
        requireNonNull(properties.getProperty(s3Endpoint()));
        requireNonNull(properties.getProperty(s3Bucket()));
    }

    private static String getFirstPropertyAvailable(Properties props, String... keys) {
        for (String key : keys) {
            String value = props.getProperty(key);
            if (value != null && !value.isEmpty()) return value;
        }
        throw new IllegalArgumentException("No valid properties found");
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

    private boolean objectExist(K key)
    {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(properties.getProperty(s3Bucket()))
                            .object(encodeFilename(key))
                            .build()
            );
        } catch (ErrorResponseException  e) {
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

    private List<String> getAllS3Objects()
    {
        // Alle Objekte im Bucket auflisten
        var results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(properties.getProperty(s3Bucket()))
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


    private String encodeFilename(K key)
    {
        return Base64.getUrlEncoder()
                .encodeToString(
                        getJSONConverter()
                                .toJson(key)
                                .getBytes(StandardCharsets.UTF_8)
                ) + ".json";
    }
}

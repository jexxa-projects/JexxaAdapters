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
import static java.util.Objects.requireNonNull;

public class S3KeyValueRepository<T,K> implements IRepository<T, K>
{
    public static final String S3_ENDPOINT = "s3.endpoint";
    public static final String S3_REGION = "s3.region";
    public static final String S3_BUCKET = "s3.bucket";
    public static final String S3_ACCESS_KEY = "s3.access-key";
    public static final String S3_SECRET_KEY = "s3.secret-key";
    public static final String S3_FILE_ACCESS_KEY = "s3.file.access-key-path";
    public static final String S3_FILE_SECRET_KEY = "s3.file.secret-key-path";

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
                .endpoint(properties.getProperty(S3_ENDPOINT))
                .credentials(
                        getFirstPropertyAvailable(properties, S3_ACCESS_KEY, S3_FILE_ACCESS_KEY),
                        getFirstPropertyAvailable(properties, S3_SECRET_KEY, S3_FILE_SECRET_KEY))
                .region(properties.getProperty(S3_REGION))
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
                            .bucket(properties.getProperty(S3_BUCKET))
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
                            .bucket(properties.getProperty(S3_BUCKET))
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
                        .bucket(properties.getProperty(S3_BUCKET))
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
                        .bucket(properties.getProperty(S3_BUCKET))
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
        requireNonNull(properties.getProperty(S3_ENDPOINT));
        requireNonNull(properties.getProperty(S3_BUCKET));
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
                    BucketExistsArgs.builder().bucket(properties.getProperty(S3_BUCKET)).build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(properties.getProperty(S3_BUCKET)).build()
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
                            .bucket(properties.getProperty(S3_BUCKET))
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
                        .bucket(properties.getProperty(S3_BUCKET))
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

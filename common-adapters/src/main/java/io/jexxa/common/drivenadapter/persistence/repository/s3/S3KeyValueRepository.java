package io.jexxa.common.drivenadapter.persistence.repository.s3;

import io.jexxa.common.drivenadapter.persistence.repository.IRepository;
import io.jexxa.common.facade.logger.SLF4jLogger;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

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

    }

    @Override
    public void remove(K key) {

    }

    @Override
    public void removeAll() {

    }

    @Override
    public void add(T aggregate) {

    }

    @Override
    public Optional<T> get(K key) {
        return Optional.empty();
    }

    @Override
    public List<T> get() {
        return List.of();
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
                // Bucket erstellen, falls nicht vorhanden
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(properties.getProperty(S3_BUCKET)).build()
                );
                SLF4jLogger.getLogger(S3KeyValueRepository.class).debug("Bucket created: {}",  properties.getProperty(S3_BUCKET));
            } else {
                SLF4jLogger.getLogger(S3KeyValueRepository.class).debug("Bucket exists: {}",  properties.getProperty(S3_BUCKET));
            }
        } catch (Exception e)
        {
            throw new IllegalArgumentException("Bucket could not be initialized! ", e);
        }

    }

}

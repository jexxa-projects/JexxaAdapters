package io.jexxa.common.facade.s3;

import io.jexxa.common.drivenadapter.persistence.repository.s3.S3KeyValueRepository;
import io.jexxa.common.facade.utils.properties.Secret;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Error;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
import static io.jexxa.common.facade.s3.S3Properties.s3ForcePathStyle;
import static io.jexxa.common.facade.s3.S3Properties.s3Region;
import static io.jexxa.common.facade.s3.S3Properties.s3SecretKey;
import static java.util.Objects.requireNonNull;

public class S3Facade {
    private final S3Client s3Client;
    private final Properties properties;

    public S3Facade(Properties properties) {
        validateProperties(properties);
        this.properties = properties;
        var s3AccessKey = new Secret(properties, s3AccessKey(), s3FileAccessKey());
        var s3SecretKey = new Secret(properties, s3SecretKey(), s3FileSecretKey());

        String regionProp = properties.getProperty(s3Region());
        Region region = (regionProp != null && !regionProp.isBlank()) ? Region.of(regionProp) : Region.US_EAST_1;

        boolean forcePathStyle = Boolean.parseBoolean(properties.getProperty(s3ForcePathStyle(), "true"));


        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(properties.getProperty(s3Endpoint())))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3AccessKey.getSecret(), s3SecretKey.getSecret())))
                .region(region)
                .httpClientBuilder(UrlConnectionHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(2))
                        .socketTimeout(Duration.ofSeconds(2)))
                .forcePathStyle(forcePathStyle)
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_SUPPORTED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_SUPPORTED)
                .build();

        initBucket();
    }

    private void initBucket() {
        String bucket = properties.getProperty(s3Bucket());
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (S3Exception e) {
            // HTTP 404 (Not Found) means bucket does not exist
            if (e.statusCode() == 404 || "NoSuchBucket".equals(e.awsErrorDetails().errorCode())) {
                try {
                    s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Bucket could not be initialized! ", ex);
                }
            } else {
                throw new IllegalArgumentException("Bucket could not be checked! S3 Error: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Bucket could not be checked! ", e);
        }
    }

    public void removeObject(String objectName) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(properties.getProperty(s3Bucket()))
                    .key(objectName)
                    .build());
        } catch (S3Exception _) {
            getLogger(S3KeyValueRepository.class).warn("Could not delete object {}", objectName);
        }
    }

    public void removeObjects(List<String> objectList) {
        // If list is empty, we return (S3 does not allow empty delete requests
        if (objectList == null || objectList.isEmpty()) {
            return;
        }

        List<ObjectIdentifier> objectsToDelete = objectList.stream()
                .map(name -> ObjectIdentifier.builder().key(name).build())
                .toList();

        try {
            DeleteObjectsResponse response = s3Client
                    .deleteObjects(req -> req
                    .bucket(properties.getProperty(s3Bucket()))
                    .delete(d -> d.objects(objectsToDelete))
            );

            if (response.hasErrors()) {
                for (S3Error error : response.errors()) {
                    getLogger(S3KeyValueRepository.class).error(
                            "Error during delete of file: {} - Error Code: {}",
                            error.key(),
                            error.code()
                    );
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Delete operation could not be executed", e);
        }
    }


    public Optional<String> get(String objectName) {
        try (var responseStream = s3Client.getObject(GetObjectRequest.builder()
                .bucket(properties.getProperty(s3Bucket()))
                .key(objectName)
                .build())) {
            return Optional.of(new String(responseStream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (NoSuchKeyException e) {
            getLogger(S3KeyValueRepository.class).debug(e.getMessage());
        } catch (RuntimeException | IOException e) {
            getLogger(S3KeyValueRepository.class).warn(e.getMessage());
        }

        return Optional.empty();
    }

    public void putObject(String objectName, InputStream objectStream, int objectSize) {
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(properties.getProperty(s3Bucket()))
                            .key(objectName)
                            .contentType("application/json")
                            .build(),
                    RequestBody.fromInputStream(objectStream, objectSize));
        } catch (S3Exception e) {
            throw new IllegalArgumentException("Could not put object with id " + objectName, e);
        }
    }

    public boolean objectExist(String objectName) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(properties.getProperty(s3Bucket()))
                    .key(objectName)
                    .build());
            return true;
        } catch (NoSuchKeyException _) {
            return false;
        } catch (S3Exception e) {
            getLogger(S3KeyValueRepository.class).warn(e.getMessage());
            return false;
        }
    }

    public List<String> getAllS3Objects() {
        return getAllS3Objects("");
    }

    public List<String> getAllS3Objects(String prefix) {
        try {
            ListObjectsV2Response response = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                    .bucket(properties.getProperty(s3Bucket()))
                    .prefix(prefix)
                    .build());

            return response
                    .contents()
                    .stream()
                    .map(S3Object::key).toList();

        } catch (S3Exception e) {
            getLogger(S3KeyValueRepository.class).warn(e.getMessage());
        }

        return Collections.emptyList();
    }

    public void removeBucket(String bucketName) {
        try {
            s3Client.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build());
        } catch (S3Exception e) {
            throw new IllegalArgumentException("Could not delete bucket " + bucketName, e);
        }
    }

    private void validateProperties(Properties properties) {
        requireNonNull(properties.getProperty(s3Endpoint()));
        requireNonNull(properties.getProperty(s3Bucket()));
    }
}

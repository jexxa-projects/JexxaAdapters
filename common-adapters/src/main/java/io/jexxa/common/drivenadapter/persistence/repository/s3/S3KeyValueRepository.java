package io.jexxa.common.drivenadapter.persistence.repository.s3;

import io.jexxa.common.drivenadapter.persistence.repository.IRepository;
import io.jexxa.common.facade.s3.S3Client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

import static io.jexxa.common.facade.json.JSONManager.getJSONConverter;
import static io.jexxa.common.facade.s3.S3Properties.s3ApplicationPrefix;
import static java.util.Objects.requireNonNull;

public class S3KeyValueRepository<T,K> implements IRepository<T, K> {
    private final Function<T, K> keyFunction;
    private final Class<T> aggregateClazz;
    private final S3Client s3Client;
    private final String s3ApplicationPrefix;

    public S3KeyValueRepository(Class<T> aggregateClazz, Function<T, K> keyFunction, Properties properties) {

        this.keyFunction = requireNonNull(keyFunction);
        this.aggregateClazz = requireNonNull(aggregateClazz);
        this.s3ApplicationPrefix = getS3ApplicationPrefix(properties);
        this.s3Client = new S3Client(properties);
    }

    @Override
    public void update(T aggregate) {
        // Datei schreiben (add oder update)
        var aggregateJSON = getJSONConverter().toJson(aggregate).getBytes(StandardCharsets.UTF_8);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(aggregateJSON)
        ) {
            s3Client.putObject(
                    encodeFilename(keyFunction.apply(aggregate)),
                    inputStream,
                    aggregateJSON.length
            );
        } catch (IOException _) {
            throw new IllegalArgumentException("Could not add aggregate with id " + encodeFilename(keyFunction.apply(aggregate)));
        }

    }

    @Override
    public void remove(K key) {
        if (!s3Client.objectExist(encodeFilename(key))) {
            throw new IllegalArgumentException("Aggregate does not exist " + key);
        }
        s3Client.removeObject(encodeFilename(key));
    }

    @Override
    public void removeAll() {
        s3Client.removeObjects(s3Client.getAllS3Objects(s3Prefix()));
    }

    @Override
    public void add(T aggregate) {
        if (s3Client.objectExist(encodeFilename(keyFunction.apply(aggregate)))) {
            throw new IllegalArgumentException("Aggregate with key " + encodeFilename(keyFunction.apply(aggregate)) + "already exists");
        }
        update(aggregate);
    }

    @Override
    public Optional<T> get(K key) {
        return s3Client.
                get(encodeFilename(key)).
                map(data -> getJSONConverter().fromJson(data, aggregateClazz));
    }

    @Override
    public List<T> get() {
        return s3Client.getAllS3Objects(s3Prefix())
                .stream()
                .map(s3Client::get)
                .flatMap(Optional::stream)
                .map(data -> getJSONConverter().fromJson(data, aggregateClazz))
                .toList();
    }


    private String encodeFilename(K key) {
        return s3Prefix() + Base64.getUrlEncoder()
                .encodeToString(
                        getJSONConverter()
                                .toJson(key)
                                .getBytes(StandardCharsets.UTF_8)
                ) + ".json";
    }

    private String getS3ApplicationPrefix(Properties properties) {
        String prefix = properties.getProperty(s3ApplicationPrefix(), "");
        if (!prefix.isEmpty() && !prefix.endsWith("/")) {
            prefix += "/";
        }
        return prefix;
    }


    private String s3Prefix()
    {
        return s3ApplicationPrefix + aggregateClazz.getSimpleName().toLowerCase()+ "/";
    }
}

package io.jexxa.common.drivenadapter.persistence.repository.s3;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jexxa.common.drivenadapter.persistence.repository.IRepository;
import io.jexxa.common.facade.s3.S3Client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static io.jexxa.common.facade.json.JSONManager.getJSONConverter;
import static io.jexxa.common.facade.s3.S3Properties.s3ApplicationPrefix;
import static java.util.Objects.requireNonNull;

public class S3CachedKeyValueRepository<T, K> implements IRepository<T, K> {
    private final Function<T, K> keyFunction;
    private final Class<T> aggregateClazz;
    private final S3Client s3Client;
    private final String s3ApplicationPrefix;
    private final String storageName;

    // Cache für Objekte: Key -> Aggregat
    private final Cache<K, T> cache;

    public S3CachedKeyValueRepository(Class<T> aggregateClazz, Function<T, K> keyFunction, Properties properties) {
        this(aggregateClazz, keyFunction, aggregateClazz.getSimpleName(), properties);
    }

    public S3CachedKeyValueRepository(Class<T> aggregateClazz, Function<T, K> keyFunction, String storageName, Properties properties) {
        this.keyFunction = requireNonNull(keyFunction);
        this.aggregateClazz = requireNonNull(aggregateClazz);
        this.storageName = storageName;
        this.s3ApplicationPrefix = getS3ApplicationPrefix(properties);
        this.s3Client = new S3Client(properties);

        // Konfiguration des Caches
        this.cache = Caffeine.newBuilder()
                .maximumSize(5000) // Begrenzung der Einträge im RAM
                .expireAfterWrite(15, TimeUnit.MINUTES) // Automatisches Ablaufen
                .build();
    }

    @Override
    public void update(T aggregate) {
        var key = keyFunction.apply(aggregate);
        var aggregateJSON = getJSONConverter().toJson(aggregate).getBytes(StandardCharsets.UTF_8);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(aggregateJSON)) {
            s3Client.putObject(encodeFilename(key), inputStream, aggregateJSON.length);
            // Cache nach erfolgreichem S3-Schreiben aktualisieren
            cache.put(key, aggregate);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not update aggregate with id " + key, e);
        }
    }

    @Override
    public void remove(K key) {
        if (get(key).isEmpty()) {
            throw new IllegalArgumentException("Aggregate does not exist " + key);
        }
        s3Client.removeObject(encodeFilename(key));
        cache.invalidate(key);
    }

    @Override
    public void removeAll() {
        s3Client.removeObjects(s3Client.getAllS3Objects(s3Prefix(storageName)));
        cache.invalidateAll();
    }

    @Override
    public void add(T aggregate) {
        K key = keyFunction.apply(aggregate);
        // Da S3 keine native "Error if exists"-Operation beim Put bietet,
        // bleibt der Check hier oft nötig, erhöht aber die Last.
        // Falls Konsistenz auf App-Ebene egal ist: Direkt update(aggregate) rufen.
        if (s3Client.objectExist(encodeFilename(key))) {
            throw new IllegalArgumentException("Aggregate with key " + key + " already exists");
        }
        update(aggregate);
    }

    @Override
    public Optional<T> get(K key) {
        // Zuerst im Cache suchen
        T cachedValue = cache.getIfPresent(key);
        if (cachedValue != null) {
            return Optional.of(cachedValue);
        }

        // Fallback auf S3
        return s3Client.get(encodeFilename(key))
                .map(data -> {
                    T aggregate = getJSONConverter().fromJson(data, aggregateClazz);
                    cache.put(key, aggregate); // Cache befüllen
                    return aggregate;
                });
    }

    @Override
    public List<T> get() {
        return s3Client.getAllS3Objects(s3Prefix(storageName))
                .parallelStream()
                .map(s3Client::get)
                .flatMap(Optional::stream)
                .map(data -> {
                    T aggregate = getJSONConverter().fromJson(data, aggregateClazz);
                    cache.put(keyFunction.apply(aggregate), aggregate); // Explizites Caching
                    return aggregate;
                })
                .toList();
    }


    private String encodeFilename(K key) {
        return s3Prefix(storageName) + Base64.getUrlEncoder()
                .withoutPadding() // Sauberere Dateinamen
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

    private String s3Prefix(String suffix) {
        return (s3ApplicationPrefix + suffix).toLowerCase() + "/";
    }
}

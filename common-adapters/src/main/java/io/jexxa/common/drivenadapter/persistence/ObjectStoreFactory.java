package io.jexxa.common.drivenadapter.persistence;


import io.jexxa.common.drivenadapter.persistence.objectstore.IObjectStore;
import io.jexxa.common.drivenadapter.persistence.objectstore.imdb.IMDBObjectStore;
import io.jexxa.common.drivenadapter.persistence.objectstore.jdbc.JDBCObjectStore;
import io.jexxa.common.drivenadapter.persistence.objectstore.metadata.MetadataSchema;
import io.jexxa.common.drivenadapter.persistence.objectstore.s3.S3ObjectStore;
import io.jexxa.common.facade.factory.ClassFactory;
import io.jexxa.common.facade.logger.ApplicationBanner;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import static io.jexxa.common.facade.jdbc.JDBCProperties.jdbcDriver;
import static io.jexxa.common.facade.jdbc.JDBCProperties.objectstoreStrategy;
import static io.jexxa.common.facade.logger.SLF4jLogger.getLogger;
import static io.jexxa.common.facade.s3.S3Properties.s3Bucket;


@SuppressWarnings({"unused", "DuplicatedCode", "java:S6548"})
public final class ObjectStoreFactory
{
    private static final ObjectStoreFactory OBJECT_STORE_FACTORY = new ObjectStoreFactory();

    private static final Map<Class<?> , Class<?>> STRATEGY_MAP = new HashMap<>();
    private static Class<?> defaultObjectStore = null;

    public static Class<?> getDefaultObjectStore(Properties properties)
    {
        return getObjectStoreType(null, properties);
    }

    @SuppressWarnings("unchecked")
    public static  <T,K,M  extends Enum<?> & MetadataSchema> IObjectStore<T,K, M> createObjectStore(
            Class<T> aggregateClazz,
            Function<T,K> keyFunction,
            Class<M> metaData,
            Properties properties)
    {
        try
        {
            var strategy = getObjectStoreType(aggregateClazz, properties);

            var result = ClassFactory.newInstanceOf(strategy, new Object[]{aggregateClazz, keyFunction, metaData, properties});

            return (IObjectStore<T, K,M>) result.orElseThrow();
        }
        catch (ReflectiveOperationException e)
        {
            if ( e.getCause() != null)
            {
                throw new IllegalArgumentException(e.getCause().getMessage(), e);
            }

            throw new IllegalArgumentException("No suitable default IRepository available", e);
        }
    }

    public static <U extends IObjectStore<?,?,?>, T > void setObjectStore(Class<U> objectStore, Class<T> aggregateType)
    {
        STRATEGY_MAP.put(aggregateType, objectStore);
    }

    public static <U extends IObjectStore<?,?,?>> void setDefaultObjectStore(Class<U> defaultObjectStore)
    {
        ObjectStoreFactory.defaultObjectStore = defaultObjectStore;
    }


    public static void defaultSettings( )
    {
        defaultObjectStore = null;
        STRATEGY_MAP.clear();
    }


    private ObjectStoreFactory()
    {
        ApplicationBanner.addConfigBanner(this::bannerInformation);
    }

    private static <T> Class<?> getObjectStoreType(Class<T> aggregateClazz, Properties properties)
    {
        // 1. Check if a dedicated strategy is registered for aggregateClazz
        var result = STRATEGY_MAP
                .entrySet()
                .stream()
                .filter( element -> element.getKey().equals(aggregateClazz))
                .filter( element -> element.getValue() != null )
                .findFirst();

        if (result.isPresent())
        {
            return result.get().getValue();
        }

        // 2. If a default strategy is available, return this one
        if (defaultObjectStore != null)
        {
            return defaultObjectStore;
        }

        // 3. Check explicit configuration
        if (properties.containsKey(objectstoreStrategy())) {
            try {
                return Class.forName(properties.getProperty(objectstoreStrategy()));
            } catch (ClassNotFoundException e) {
                getLogger(ObjectStoreFactory.class).warn("Unknown or invalid object store {} -> Ignore setting", properties.getProperty(objectstoreStrategy()));
            }
        }

        // 4. If a JDBC driver is stated in Properties => Use JDBCKeyValueRepository
        if (properties.containsKey(jdbcDriver()))
        {
            return JDBCObjectStore.class;
        }

        // 5. If a S3 bucket is stated in Properties => Use S3ObjectStore
        if (properties.containsKey(s3Bucket()))
        {
            return S3ObjectStore.class;
        }

        // 6. If everything fails, return an IMDBObjectStore
        return IMDBObjectStore.class;
    }

    public void bannerInformation(Properties properties)
    {
        getLogger(ApplicationBanner.class).info("Used ObjectStore Strategie     : [{}]",getDefaultObjectStore(properties).getSimpleName());
    }
}

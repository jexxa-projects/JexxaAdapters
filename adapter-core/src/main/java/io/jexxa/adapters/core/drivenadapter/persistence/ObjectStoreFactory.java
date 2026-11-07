package io.jexxa.adapters.core.drivenadapter.persistence;


import io.jexxa.adapters.core.drivenadapter.persistence.objectstore.IObjectStore;
import io.jexxa.adapters.core.drivenadapter.persistence.objectstore.imdb.IMDBObjectStore;
import io.jexxa.adapters.core.drivenadapter.persistence.objectstore.metadata.MetadataSchema;
import io.jexxa.adapters.core.facade.factory.ClassFactory;
import io.jexxa.adapters.core.facade.logger.ApplicationBanner;
import io.jexxa.adapters.core.facade.logger.SLF4jLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.function.Function;

import static io.jexxa.adapters.core.drivenadapter.persistence.PersistenceProperties.objectstoreStrategy;
import static io.jexxa.adapters.core.facade.logger.SLF4jLogger.getLogger;


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

    public static  <T,K,M  extends Enum<?> & MetadataSchema> IObjectStore<T,K, M> createObjectStore(
            Class<T> aggregateClazz,
            Function<T,K> keyFunction,
            Class<M> metaData,
            Properties properties)
    {
        return createObjectStore(aggregateClazz, keyFunction, aggregateClazz.getSimpleName(), metaData, properties);
    }

    @SuppressWarnings("unchecked")
    public static  <T,K,M  extends Enum<?> & MetadataSchema> IObjectStore<T,K, M> createObjectStore(
            Class<T> aggregateClazz,
            Function<T,K> keyFunction,
            String storageName,
            Class<M> metaData,
            Properties properties)
    {
        try
        {
            var strategy = getObjectStoreType(aggregateClazz, properties);

            var objectStore = (IObjectStore<T, K,M>)(ClassFactory.newInstanceOf(
                    strategy,
                    new Object[]{aggregateClazz, keyFunction, storageName, metaData, properties}
                )).orElseThrow();

            objectStore.init();

            return objectStore;
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
            } catch (ClassNotFoundException _) {
                getLogger(ObjectStoreFactory.class).warn("Unknown or invalid object store {} -> Ignore setting", properties.getProperty(objectstoreStrategy()));
            }
        }

        // 4. Discover matching repository providers
        for (var provider : ServiceLoader.load(ObjectStoreProvider.class))
        {
            var objectStoreType = provider.getObjectStoreType(properties);

            if (objectStoreType.isPresent())
            {
                SLF4jLogger.getLogger(ObjectStoreFactory.class).info("Using object store type {}", objectStoreType);
                return objectStoreType.get();
            }
        }

        // 6. If everything fails, return an IMDBObjectStore
        return IMDBObjectStore.class;
    }

    public void bannerInformation(Properties properties)
    {
        getLogger(ApplicationBanner.class).info("Used ObjectStore Strategie     : [{}]",getDefaultObjectStore(properties).getSimpleName());
    }
}

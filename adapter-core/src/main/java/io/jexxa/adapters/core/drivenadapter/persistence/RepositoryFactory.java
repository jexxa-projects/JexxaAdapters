package io.jexxa.adapters.core.drivenadapter.persistence;


import io.jexxa.adapters.core.drivenadapter.persistence.repository.IRepository;
import io.jexxa.adapters.core.drivenadapter.persistence.repository.imdb.IMDBRepository;
import io.jexxa.adapters.core.facade.factory.ClassFactory;
import io.jexxa.adapters.core.facade.logger.ApplicationBanner;
import io.jexxa.adapters.core.facade.utils.annotation.CheckReturnValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.function.Function;

import static io.jexxa.adapters.core.drivenadapter.persistence.PersistenceProperties.repositoryStrategy;
import static io.jexxa.adapters.core.facade.logger.SLF4jLogger.getLogger;


@SuppressWarnings({"unused", "java:S6548"})
public final class RepositoryFactory
{
    private static final RepositoryFactory REPOSITORY_FACTORY = new RepositoryFactory();

    private static final Map<Class<?> , Class<?>> STRATEGY_MAP = new HashMap<>();
    private static Class<?> defaultStrategy = null;

    private RepositoryFactory()
    {
        ApplicationBanner.addConfigBanner(this::bannerInformation);
    }

    /**
     * Determines and returns the repository class to be used for the given aggregate type.
     * <p>
     * The strategy resolution follows a fixed priority order:
     * </p>
     * <ol>
     *   <li>
     *     Check whether a dedicated repository strategy is explicitly registered
     *     for the given aggregate class {@link #setRepository(Class, Class)}.
     *   </li>
     *   <li>
     *     If no dedicated strategy is found and a default strategy is configured,
     *     return the default strategy.{@link #setDefaultRepository(Class)}
     *   </li>
     *   <li>
     *     If a JDBC driver is configured in the properties, use the
     *     {@code JDBCKeyValueRepository} as repository implementation.
     *   </li>
     *   <li>
     *     If an S3 bucket is configured in the properties, use the
     *     {@code S3KeyValueRepository} as repository implementation.
     *   </li>
     *   <li>
     *     As a final fallback, if none of the above conditions apply, an
     *     in-memory repository implementation is used.
     *   </li>
     * </ol>
     *
     * @return the repository strategy class to be used
     */
    @CheckReturnValue
    public static  <T,K> IRepository<T,K> createRepository(
            Class<T> aggregateClazz,
            Function<T,K> keyFunction,
            Properties properties)
    {
        return createRepository(aggregateClazz, keyFunction, aggregateClazz.getSimpleName(), properties);
    }


    @SuppressWarnings("unchecked")
    public static  <T,K> IRepository<T,K> createRepository(
            Class<T> aggregateClazz,
            Function<T,K> keyFunction,
            String storageName,
            Properties properties)
    {
        try
        {
            var strategy = getRepositoryType(aggregateClazz, properties);

            var repository = (IRepository<T, K>)(ClassFactory.newInstanceOf(
                    strategy,
                    new Object[]{aggregateClazz, keyFunction, storageName, properties}
            )).orElseThrow();

            repository.init();

            return repository;
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

    public static <U extends IRepository<?,?>, T > void setRepository(Class<U> repositoryType, Class<T> aggregateType)
    {
        STRATEGY_MAP.put(aggregateType, repositoryType);
    }

    public static <U extends IRepository<?,?> > void setDefaultRepository(Class<U> defaultStrategy)
    {
        RepositoryFactory.defaultStrategy = defaultStrategy;
    }
    public static Class<?> getDefaultRepository(Properties properties)
    {
        return getRepositoryType(null, properties);
    }

    @SuppressWarnings("unchecked")
    @CheckReturnValue
    private <T,K> IRepository<T,K> getRepositoryType(
            Class<T> aggregateClazz,
            Function<T,K> keyFunction,
            Properties properties
    )
    {
        try
        {
            var strategy = getRepositoryType(aggregateClazz, properties);

            var result = ClassFactory.newInstanceOf(strategy, new Object[]{aggregateClazz, keyFunction, properties});

            return (IRepository<T, K>) result.orElseThrow();
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

    public static void defaultSettings( )
    {
        defaultStrategy = null;
        STRATEGY_MAP.clear();
    }

    private static <T> Class<?> getRepositoryType(
            Class<T> aggregateClazz,
            Properties properties)
    {
        // 1. Dedicated strategy
        var strategy = STRATEGY_MAP.get(aggregateClazz);

        if (strategy != null)
        {
            return strategy;
        }

        // 2. Default strategy
        if (defaultStrategy != null)
        {
            return defaultStrategy;
        }

        // 3. Check explicit configuration
        if (properties.containsKey(repositoryStrategy())) {
            try {
                return Class.forName(properties.getProperty(repositoryStrategy()));
            } catch (ClassNotFoundException _) {
                getLogger(RepositoryFactory.class).warn("Unknown or invalid repository {} -> Ignore setting", properties.getProperty(repositoryStrategy()));
            }
        }

        // 3. Discover matching repository providers
        for (var provider : ServiceLoader.load(RepositoryProvider.class))
        {
            var repositoryType = provider.getRepositoryType(properties);

            if (repositoryType.isPresent())
            {
                return repositoryType.get();
            }
        }

        // 4. Fallback
        return IMDBRepository.class;
    }

    public void bannerInformation(Properties properties)
    {
        getLogger(ApplicationBanner.class).info("Used Repository Strategie      : [{}]",getDefaultRepository(properties).getSimpleName());
    }
}

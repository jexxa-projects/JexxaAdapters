package io.jexxa.common.drivenadapter.persistence;


import io.jexxa.common.drivenadapter.persistence.repository.IRepository;
import io.jexxa.common.drivenadapter.persistence.repository.imdb.IMDBRepository;
import io.jexxa.common.drivenadapter.persistence.repository.jdbc.JDBCKeyValueRepository;
import io.jexxa.common.drivenadapter.persistence.repository.s3.S3KeyValueRepository;
import io.jexxa.common.facade.factory.ClassFactory;
import io.jexxa.common.facade.jdbc.JDBCProperties;
import io.jexxa.common.facade.logger.ApplicationBanner;
import io.jexxa.common.facade.utils.annotation.CheckReturnValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import static io.jexxa.common.facade.jdbc.JDBCProperties.repositoryStrategy;
import static io.jexxa.common.facade.logger.SLF4jLogger.getLogger;
import static io.jexxa.common.facade.s3.S3Properties.s3Bucket;


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
    @SuppressWarnings("unchecked")
    @CheckReturnValue
    public static  <T,K> IRepository<T,K> createRepository(
            Class<T> aggregateClazz,
            Function<T,K> keyFunction,
            Properties properties)
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


    @SuppressWarnings("DuplicatedCode")
    private static <T> Class<?> getRepositoryType(Class<T> aggregateClazz, Properties properties)
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
        // 4. If a JDBC driver is stated in Properties => Use JDBCKeyValueRepository
        if (properties.containsKey(JDBCProperties.jdbcDriver()))
        {
            return JDBCKeyValueRepository.class;
        }

        // 5. If a S3 bucket is stated in Properties => Use S3KeyValueRepository
        if (properties.containsKey(s3Bucket()))
        {
            return S3KeyValueRepository.class;
        }

        // 6. If everything fails, return an IMDBRepository
        return IMDBRepository.class;
    }
    public void bannerInformation(Properties properties)
    {
        getLogger(ApplicationBanner.class).info("Used Repository Strategie      : [{}]",getDefaultRepository(properties).getSimpleName());
    }
}

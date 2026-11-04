package io.jexxa.adapterapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * The {@code JexxaContext} class provides a global, singleton-based context for managing
 * initialization, validation, and cleanup handlers within a Jexxa application.
 * <p>
 * This class allows different parts of the application to register callbacks that are executed
 * during initialization, validation of configuration properties, and cleanup.
 * <ul>
 *     <li><b>Init Handlers</b> are executed in the order in which they are registered</li>
 *     <li><b>Cleanup Handlers</b> are executed in reverse order of registration to avoid dependency issues</li>
 *     <li><b>Validation Handlers</b> receive configuration properties and can validate them</li>
 * </ul>
 * <p>
 * The class uses the Singleton pattern to maintain a single shared instance and cannot be instantiated externally.
 */
@SuppressWarnings("unused")
public class JexxaContext {
    private static final JexxaContext INSTANCE = new JexxaContext();

    private final List<Runnable> cleanupHandler = Collections.synchronizedList(new ArrayList<>());
    private final List<Runnable> initHandler = Collections.synchronizedList(new ArrayList<>());
    private final List<Consumer<Properties>> validationHandler = Collections.synchronizedList(new ArrayList<>());

    /**
     * Registers a new initialization handler.
     * <p>
     * The handler will be executed when {@link #init()} is called, in the same order
     * in which handlers are registered.
     *
     * @param initHandler the initialization logic to register
     */
    public static void registerInitHandler(Runnable initHandler)
    {
        INSTANCE.initHandler.add(initHandler);
    }

    /**
     * Registers a new cleanup handler.
     * <p>
     * Cleanup handlers are stored in reversed execution order: the most recently registered
     * handler will be executed first when {@link #cleanup()} is called.
     * <p>
     * This prevents dependency issues between resources that require shutdown in reverse order.
     * <pre>
     * Example:
     * - JDBCPool registered first
     * - RepositoryPool registered second
     *
     * Cleanup order must be:
     * 1. RepositoryPool
     * 2. JDBCPool
     * </pre>
     *
     * @param cleanupHandler the cleanup logic to register
     */
    public static void registerCleanupHandler(Runnable cleanupHandler)
    {
        //Cleanup handlers are registered in the order they were created. Thus, they must be called in reverse order
        //to avoid problems with dependencies between resources.
        //Example: RepositoryPool -depends on-> JDBCPool.
        //     The registration order is JDBCPool, RepositoryPool.
        //     Cleanup order must be RepositoryPool, JDBCPool.
        // To avoid reverting the cleanup handler, we add a new handler at the beginning of the list
        INSTANCE.cleanupHandler.addFirst(cleanupHandler);
    }

    /**
     * Registers a handler that validates configuration settings.
     * <p>
     * Validation handlers are executed when {@link #validate(Properties)} is called
     * and receive the provided {@link Properties} instance.
     *
     * @param validationHandler a consumer that validates application properties
     */
    public static void registerValidationHandler(Consumer<Properties> validationHandler)
    {
        INSTANCE.validationHandler.add(validationHandler);
    }


    /**
     * Executes all registered cleanup handlers in the correct order.
     * <p>
     * Handlers are executed in reverse order of registration.
     */
    public static void cleanup()
    {

        INSTANCE.cleanupHandler.forEach(Runnable::run);
    }


    /**
     * Executes all registered initialization handlers in registration order.
     */
    public static void init()
    {
        INSTANCE.initHandler.forEach(Runnable::run);
    }

    /**
     * Executes all registered validation handlers using the provided configuration properties.
     * <p>
     * Each registered validation handler receives the given {@link Properties} instance and may
     * perform checks to ensure the application is correctly configured. If any validation fails,
     * the handler is expected to throw an exception. This method will convert such exceptions into
     * a {@link ConfigurationFailedException}.
     * <p>
     * A failure during validation is considered a critical and unrecoverable error. Therefore,
     * this method intentionally throws a runtime exception to enforce immediate termination of the
     * application startup process rather than allowing the caller to recover from it.
     *
     * @param properties the configuration properties to validate
     * @throws ConfigurationFailedException if any registered validation handler reports a failure
     */
    public static void validate(Properties properties)
    {
        INSTANCE.validationHandler.forEach(handler -> {
            try {
                handler.accept(properties);
            } catch ( ConfigurationFailedException e) // If we get this exception type, we just forward the exception
            {
                throw e;
            }   catch (Exception e) { //In all other cases, we convert it into a ConfigurationFailedException
                throw new ConfigurationFailedException("Configuration validation failed. Reason: " + e.getMessage(), e);
            }
        });
    }

    private JexxaContext()
    {
        //Private constructor to ensure Singleton
    }
}

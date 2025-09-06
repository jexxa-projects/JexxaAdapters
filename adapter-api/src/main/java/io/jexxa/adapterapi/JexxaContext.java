package io.jexxa.adapterapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

public class JexxaContext {
    private static final JexxaContext INSTANCE = new JexxaContext();

    private final List<Runnable> cleanupHandler = new ArrayList<>();
    private final List<Runnable> initHandler = new ArrayList<>();
    private final List<Consumer<Properties>> validationHandler = new ArrayList<>();

    public static void registerInitHandler(Runnable initHandler)
    {
        INSTANCE.initHandler.add(initHandler);
    }

    public static void registerCleanupHandler(Runnable cleanupHandler)
    {
        //Cleanup handlers are registered in the order they were created. Thus, they must be called in reverse order
        //to avoid problems with dependencies between resources.
        //Example: RepositoryPool -depends on-> JDBCPool.
        //     The registration order is JDBCPool, RepositoryPool.
        //     Cleanup order must be RepositoryPool, JDBCPool.
        // To avoid reverting the cleanup handler, we add a new handler at the beginning of the list
        INSTANCE.cleanupHandler.add(0, cleanupHandler);
    }

    /** Register a handler that accepts Properties-settings to validate its configuration
    */
    public static void registerValidationHandler(Consumer<Properties> validationHandler)
    {
        INSTANCE.validationHandler.add(validationHandler);
    }


    public static void cleanup()
    {

        INSTANCE.cleanupHandler.forEach(Runnable::run);
    }

    public static void init()
    {
        INSTANCE.initHandler.forEach(Runnable::run);
    }

    public static void validate(Properties properties)
    {
        INSTANCE.validationHandler.forEach(element -> element.accept( properties ));
    }

    private JexxaContext()
    {
        //Private constructor to ensure Singleton
    }
}

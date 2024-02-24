package io.jexxa.adapterapi;

import java.util.ArrayList;
import java.util.List;

public class JexxaContext {
    private static final JexxaContext INSTANCE = new JexxaContext();

    private final List<Runnable> cleanupHandler = new ArrayList<>();
    private final List<Runnable> initHandler = new ArrayList<>();

    public static void registerInitHandler(Runnable initHandler)
    {
        INSTANCE.initHandler.add(initHandler);
    }

    public static void registerCleanupHandler(Runnable cleanupHandler)
    {
        //Cleanup handlers are registered in the order they were created. Thus, they must be called in reverse order
        //to avoid problems with dependencies between resources.
        //Example: RepositoryPool -depends on-> JDBCPool.
        //     Registration order is JDBCPool, RepositoryPool.
        //     Cleanup order must be RepositoryPool, JDBCPool.
        // To avoid reverting the cleanup handler, we add a new handler at the beginning of the list
        INSTANCE.cleanupHandler.add(0, cleanupHandler);
    }

    public static void cleanup()
    {

        INSTANCE.cleanupHandler.forEach(Runnable::run);
    }

    public static void init()
    {
        INSTANCE.initHandler.forEach(Runnable::run);
    }

    private JexxaContext()
    {
        //Private constructor to ensure Singleton
    }
}

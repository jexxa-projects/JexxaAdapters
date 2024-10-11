package io.jexxa.adapterapi.invocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InvocationManager {
    private static final Map<Object, JexxaInvocationHandler> INVOCATION_HANDLER_MAP = new ConcurrentHashMap<>();
    private static JexxaInvocationHandler defaultInvocationHandler = new TransactionalInvocationHandler();

    public static void setInvocationHandler(JexxaInvocationHandler invocationHandler, Object object)
    {
        INVOCATION_HANDLER_MAP.put(object, invocationHandler);
    }

    public static void setDefaultInvocationHandler(JexxaInvocationHandler defaultInvocationHandler)
    {
        InvocationManager.defaultInvocationHandler = defaultInvocationHandler;
    }

    public static synchronized JexxaInvocationHandler getInvocationHandler(Object object)
    {
        return INVOCATION_HANDLER_MAP.computeIfAbsent(object, key -> createDefaultInvocationHandler());
    }

    public static JexxaInvocationHandler getRootInterceptor(Object object)
    {
        return getInvocationHandler(object);
    }

    private static JexxaInvocationHandler createDefaultInvocationHandler()
    {
        return defaultInvocationHandler.newInstance();
    }

    private InvocationManager()
    {
        //private constructor
    }
}

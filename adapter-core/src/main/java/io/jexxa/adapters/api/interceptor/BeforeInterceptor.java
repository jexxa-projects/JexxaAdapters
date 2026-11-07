package io.jexxa.adapters.api.interceptor;

import io.jexxa.adapters.api.invocation.InvocationContext;

public interface BeforeInterceptor {
    void before(InvocationContext invocationContext);
}

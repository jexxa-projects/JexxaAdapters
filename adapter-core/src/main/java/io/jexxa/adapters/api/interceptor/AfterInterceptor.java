package io.jexxa.adapters.api.interceptor;

import io.jexxa.adapters.api.invocation.InvocationContext;

public interface AfterInterceptor {
    void after(InvocationContext invocationContext);
}

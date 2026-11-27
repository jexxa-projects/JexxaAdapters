package io.jexxa.adapters.api.interceptor;

import io.jexxa.adapters.api.invocation.InvocationContext;

@SuppressWarnings("unused")
public class DefaultInterceptor implements AroundInterceptor, BeforeInterceptor, AfterInterceptor
{

    @Override
    public void before(InvocationContext invocationContext) { /* default implementation */ }

    @Override
    public void after(InvocationContext invocationContext) { /* default implementation */ }

    @Override
    public void around(InvocationContext invocationContext)
    {
        invocationContext.proceed();
    }
}

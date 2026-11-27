package io.jexxa.adapters.api.invocation;

import io.jexxa.adapters.api.interceptor.AfterInterceptor;
import io.jexxa.adapters.api.interceptor.AroundInterceptor;
import io.jexxa.adapters.api.interceptor.BeforeInterceptor;
import io.jexxa.adapters.api.invocation.context.ConsumerInvocationContext;
import io.jexxa.adapters.api.invocation.context.FunctionInvocationContext;
import io.jexxa.adapters.api.invocation.context.MethodInvocationContext;
import io.jexxa.adapters.api.invocation.context.RunnableInvocationContext;
import io.jexxa.adapters.api.invocation.context.SupplierInvocationContext;
import io.jexxa.adapters.api.invocation.function.SerializableConsumer;
import io.jexxa.adapters.api.invocation.function.SerializableFunction;
import io.jexxa.adapters.api.invocation.function.SerializableRunnable;
import io.jexxa.adapters.api.invocation.function.SerializableSupplier;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public class SharedInvocationHandler implements AroundInterceptor, BeforeInterceptor, AfterInterceptor, JexxaInvocationHandler {
    private final List<BeforeInterceptor> beforeList = new ArrayList<>();
    private final List<AfterInterceptor> afterList = new ArrayList<>();
    private final List<AroundInterceptor> aroundList = new ArrayList<>();

    @Override
    public void before(InvocationContext invocationContext)
    {
        beforeList.forEach(element -> element.before(invocationContext));
    }

    @Override
    public void after(InvocationContext invocationContext)
    {
        afterList.forEach(element -> element.after(invocationContext));
    }

    @Override
    public void around(InvocationContext invocationContext)
    {
        invocationContext.proceed();
    }

    @Override
    public SharedInvocationHandler registerAround(AroundInterceptor interceptor) {
        aroundList.add(interceptor);
        return this;
    }

    @Override
    public SharedInvocationHandler registerBefore(BeforeInterceptor interceptor) {
        beforeList.add(interceptor);
        return this;
    }

    @Override
    public SharedInvocationHandler registerAfter(AfterInterceptor interceptor) {
        afterList.add(interceptor);
        return this;
    }


    @Override
    public Object invoke(Method method, Object object, Object[] args)
    {
        var invocationContext = new MethodInvocationContext(method, object, args, aroundList);

        invoke(invocationContext);

        return invocationContext.getReturnValue();
    }

    @Override
    public void invoke(Object targetObject, SerializableRunnable runnable)
    {
        var invocationContext = new RunnableInvocationContext(targetObject, runnable, aroundList);
        invoke(invocationContext);
    }

    @Override
    public <T> void invoke(Object targetObject,SerializableConsumer<T> consumer, T argument)
    {
        var invocationContext = new ConsumerInvocationContext<>(targetObject, consumer, argument, aroundList);
        invoke(invocationContext);
    }

    @Override
    public <T> T invoke(Object targetObject,SerializableSupplier<T> supplier) {
        var invocationContext = new SupplierInvocationContext<>(targetObject, supplier, aroundList);
        invoke(invocationContext);
        return invocationContext.getReturnValue();
    }

    @Override
    public JexxaInvocationHandler newInstance() {
        return new SharedInvocationHandler();
    }

    @Override
    public <T, R> R invoke(Object targetObject,SerializableFunction<T, R> function, T argument) {
        var invocationContext = new FunctionInvocationContext<>(targetObject, function, argument, aroundList);
        invoke(invocationContext);
        return invocationContext.getReturnValue();
    }

    protected void invoke(InvocationContext invocationContext)  {
        before(invocationContext);
        around(invocationContext);
        after(invocationContext);
    }

}

package io.jexxa.adapters.api.invocation;

import io.jexxa.adapters.api.interceptor.AfterInterceptor;
import io.jexxa.adapters.api.interceptor.AroundInterceptor;
import io.jexxa.adapters.api.interceptor.BeforeInterceptor;
import io.jexxa.adapters.api.invocation.transaction.TransactionManager;

@SuppressWarnings("UnusedReturnValue")
public class TransactionalInvocationHandler
        extends SharedInvocationHandler
        implements AroundInterceptor, BeforeInterceptor, AfterInterceptor
{

    private static final Object GLOBAL_SYNCHRONIZATION_OBJECT = new Object();


    @Override
    public JexxaInvocationHandler newInstance() {
        return new TransactionalInvocationHandler();
    }

    @Override
    protected void invoke(InvocationContext invocationContext)  {
        synchronized (GLOBAL_SYNCHRONIZATION_OBJECT)
        {
            try {
                TransactionManager.initTransaction();
                super.invoke(invocationContext);
                TransactionManager.closeTransaction();
            } catch (Exception e) {
                TransactionManager.rollback();
                TransactionManager.closeTransaction();
                throw e;
            }
        }
    }
}

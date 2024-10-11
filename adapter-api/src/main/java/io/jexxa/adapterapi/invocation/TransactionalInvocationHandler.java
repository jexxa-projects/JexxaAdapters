package io.jexxa.adapterapi.invocation;

import io.jexxa.adapterapi.interceptor.AfterInterceptor;
import io.jexxa.adapterapi.interceptor.AroundInterceptor;
import io.jexxa.adapterapi.interceptor.BeforeInterceptor;
import io.jexxa.adapterapi.invocation.transaction.TransactionManager;

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

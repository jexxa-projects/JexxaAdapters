package io.jexxa.adapters.api.invocation.transaction;

public interface TransactionHandler {
    void initTransaction();
    void closeTransaction();

    void rollback();
}

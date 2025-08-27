package io.jexxa.common.drivenadapter.persistence.repository.jdbc;

import io.jexxa.adapterapi.invocation.transaction.TransactionHandler;
import io.jexxa.adapterapi.invocation.transaction.TransactionManager;
import io.jexxa.common.facade.jdbc.JDBCConnection;

import java.util.Objects;
import java.util.Properties;

import static io.jexxa.common.facade.jdbc.JDBCConnectionPool.getJDBCConnection;
import static io.jexxa.common.facade.logger.SLF4jLogger.getLogger;

public abstract class JDBCRepository implements TransactionHandler {
    private final Properties properties;

    protected JDBCRepository(Properties properties)
    {
        this.properties = Objects.requireNonNull(properties);
        TransactionManager.registerTransactionHandler(this);
    }

    /**
     * Returns a JDBCConnection that is in a valid state. If the connection cannot be changed into a valid state, an IllegalStateException is thrown.
     *
     * @throws IllegalStateException if JDBCConnection cannot be reset
     * @return JDBCConnection that is in a valid state.
     */
    public JDBCConnection getConnection()
    {
        return getJDBCConnection(properties, this);
    }
    @Override
    public void initTransaction()
    {
        getConnection().disableAutoCommit();
    }
    @Override
    public void closeTransaction()
    {
        getConnection().commit();
        getConnection().enableAutoCommit();
    }

    @Override
    public void rollback()
    {
        try {
            getConnection().rollback();
        } catch (IllegalStateException e)
        {
            getLogger(getClass()).error("An exception occurred during rollback. Reason: {}", e.getMessage());
        }
    }
}

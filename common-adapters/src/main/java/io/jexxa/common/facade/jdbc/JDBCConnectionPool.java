package io.jexxa.common.facade.jdbc;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static io.jexxa.adapterapi.JexxaContext.registerCleanupHandler;

@SuppressWarnings({"java:S6548", "unused"})
public final class JDBCConnectionPool implements AutoCloseable {

    private static final JDBCConnectionPool JDBC_CONNECTION_POOL = new JDBCConnectionPool();
    private static boolean isConnectionSharingEnabled = false;

    // Concurrent maps are used to handle optimized concurrent access to this class without synchronizing each method
    // This is possible because methods that are accessed in parallel are synchronized by the maps (such as computeIfAbsent)
    private final Map<String, JDBCConnection> sharedConnectionMap = new ConcurrentHashMap<>();
    private final Map<Object, JDBCConnection> exclusiveConnectionMap = new ConcurrentHashMap<>();
    private final Map<Object, JDBCConnection.IsolationLevel> connectionConfiguration = new ConcurrentHashMap<>();


    public static void disableConnectionSharing() { isConnectionSharingEnabled = false; }

    // This is only meaningful if your application runs only a single thread
    public static void enableConnectionSharing() { isConnectionSharingEnabled = true; }

    public static synchronized JDBCConnection getJDBCConnection(Properties properties, Object managingObject)
    {
        var connectionName = properties.getProperty(JDBCProperties.jdbcUrl());

        if ( connectionName == null )
        {
            throw new IllegalArgumentException("Parameter " + JDBCProperties.jdbcUrl() + " is missing");
        }

        if (!isConnectionSharingEnabled || JDBC_CONNECTION_POOL.requiresExclusiveConnection(managingObject))
        {
            return JDBC_CONNECTION_POOL.getExclusiveConnection(properties, managingObject);
        }
        return JDBC_CONNECTION_POOL.getSharedConnection(properties, connectionName);
    }

    public static boolean validateJDBCConnection(Properties properties)
    {
        try(var connection = new JDBCConnection(properties))
        {
            return connection.isValid();
        }
    }

    public static void configureExclusiveConnection(Object managingObject, JDBCConnection.IsolationLevel isolationLevel)
    {
        JDBC_CONNECTION_POOL.connectionConfiguration.put(managingObject, isolationLevel);
    }


    private JDBCConnectionPool()
    {
        registerCleanupHandler(this::close);
    }



    private boolean requiresExclusiveConnection(Object managingObject)
    {
        return connectionConfiguration.containsKey(managingObject);
    }

    private JDBCConnection getSharedConnection(Properties properties, String connectionName)
    {
        return sharedConnectionMap
                .computeIfAbsent(connectionName, key -> new JDBCConnection(properties))
                .validateConnection();
    }

    private JDBCConnection getExclusiveConnection(Properties properties, Object managingObject)
    {
        return exclusiveConnectionMap
                .computeIfAbsent(managingObject, key -> {
                    var jdbcConnection = new JDBCConnection(properties);
                    jdbcConnection.setIsolationLevel(connectionConfiguration.get(managingObject));
                    return jdbcConnection;
                })
                .validateConnection();
    }

    @Override
    public void close() {
        sharedConnectionMap.forEach( ((s, jdbcConnection) -> jdbcConnection.close()));
        sharedConnectionMap.clear();

        exclusiveConnectionMap.forEach( ((s, jdbcConnection) -> jdbcConnection.close()));
        exclusiveConnectionMap.clear();
    }
}

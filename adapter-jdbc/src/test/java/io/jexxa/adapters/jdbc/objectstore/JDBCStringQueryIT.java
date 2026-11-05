package io.jexxa.adapters.jdbc.objectstore;


import io.jexxa.adapters.jdbc.facade.JDBCConnection;
import io.jexxa.adapters.jdbc.facade.JDBCTestDatabase;
import io.jexxa.common.drivenadapter.persistence.objectstore.IObjectStoreIT;
import io.jexxa.common.drivenadapter.persistence.objectstore.IStringQueryIT;
import io.jexxa.common.drivenadapter.persistence.objectstore.TestObject;

import java.util.Properties;
import java.util.stream.Stream;

import static io.jexxa.adapters.jdbc.facade.JDBCProperties.jdbcUrl;

class JDBCStringQueryIT extends IStringQueryIT
{
    @Override
    protected void reset(Properties properties)
    {
        if (properties.containsKey(jdbcUrl()))
        {
            try(JDBCConnection jdbcConnection = new JDBCConnection(properties))
            {
                jdbcConnection.tableCommand(IObjectStoreIT.TestObjectSchema.class)
                        .dropTableIfExists(TestObject.class)
                        .asIgnore();
            }
        }
    }

    @SuppressWarnings("unused")
    public static Stream<Properties> objectStoreConfig() {
        return JDBCTestDatabase.repositoryConfigJDBC();
    }
}

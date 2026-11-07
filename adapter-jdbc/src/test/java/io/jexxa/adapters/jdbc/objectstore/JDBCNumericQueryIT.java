package io.jexxa.adapters.jdbc.objectstore;


import io.jexxa.adapters.jdbc.facade.JDBCConnection;
import io.jexxa.adapters.jdbc.facade.JDBCTestDatabase;
import io.jexxa.adapters.core.drivenadapter.persistence.objectstore.INumericQueryIT;
import io.jexxa.adapters.core.drivenadapter.persistence.objectstore.TestObject;

import java.util.Properties;
import java.util.stream.Stream;

import static io.jexxa.adapters.jdbc.facade.JDBCProperties.jdbcUrl;

class JDBCNumericQueryIT extends INumericQueryIT
{
    @Override
    protected void reset(Properties properties) {
        if (properties.containsKey(jdbcUrl())) {
            try (JDBCConnection jdbcConnection = new JDBCConnection(properties)) {
                jdbcConnection.tableCommand(TestObjectSchema.class)
                        .dropTableIfExists(TestObject.class)
                        .asIgnore();

            }
        }
    }

    @SuppressWarnings("unused")
    private static Stream<Properties> objectStoreConfig() {
        return JDBCTestDatabase.repositoryConfigJDBC();
    }
}

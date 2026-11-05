package io.jexxa.adapters.jdbc.objectstore;


import io.jexxa.adapters.jdbc.facade.JDBCConnection;
import io.jexxa.adapters.jdbc.facade.JDBCTestDatabase;
import io.jexxa.common.drivenadapter.persistence.objectstore.IObjectStoreIT;
import io.jexxa.common.drivenadapter.persistence.objectstore.TestObject;
import io.jexxa.common.testapplication.TestConstants;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Properties;
import java.util.stream.Stream;

import static io.jexxa.adapters.jdbc.facade.JDBCProperties.jdbcUrl;

@Execution(ExecutionMode.SAME_THREAD)
@Tag(TestConstants.INTEGRATION_TEST)
class JDBCObjectStoreIT extends IObjectStoreIT
{
    @Override
    protected void reset(Properties properties)
    {
        if (properties.containsKey(jdbcUrl()))
        {
            try(JDBCConnection jdbcConnection = new JDBCConnection(properties))
            {
                jdbcConnection.tableCommand(TestObjectSchema.class)
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

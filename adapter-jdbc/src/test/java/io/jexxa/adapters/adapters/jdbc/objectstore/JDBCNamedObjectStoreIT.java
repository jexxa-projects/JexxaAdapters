package io.jexxa.adapters.adapters.jdbc.objectstore;


import io.jexxa.adapters.adapters.jdbc.facade.JDBCConnection;
import io.jexxa.adapters.adapters.jdbc.facade.JDBCTestDatabase;
import io.jexxa.adapters.adapters.jdbc.repository.JDBCKeyValueRepository;
import io.jexxa.adapters.core.drivenadapter.persistence.objectstore.INamedObjectStoreIT;
import io.jexxa.adapters.testapplication.TestConstants;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Properties;
import java.util.stream.Stream;

@Execution(ExecutionMode.SAME_THREAD)
@Tag(TestConstants.INTEGRATION_TEST)
class JDBCNamedObjectStoreIT extends INamedObjectStoreIT {
    @Override
    protected void reset(Properties properties)
    {
        try (JDBCConnection connection = new JDBCConnection(properties) ) {
            connection.tableCommand(JDBCKeyValueRepository.KeyValueSchema.class)
                    .dropTableIfExists(TABLE_NAME)
                    .asIgnore();
        }
    }
    @SuppressWarnings("unused")
    private static Stream<Properties> objectStoreConfig() {
        return JDBCTestDatabase.repositoryConfigJDBC();
    }
}

package io.jexxa.adapters.jdbc.repository;

import io.jexxa.adapters.jdbc.facade.JDBCConnection;
import io.jexxa.adapters.jdbc.facade.JDBCTestDatabase;
import io.jexxa.common.drivenadapter.persistence.repository.TestEntityRepositoryImplIT;
import io.jexxa.common.testapplication.TestAggregate;
import io.jexxa.common.testapplication.TestConstants;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Properties;
import java.util.stream.Stream;


@Execution(ExecutionMode.SAME_THREAD)
@Tag(TestConstants.INTEGRATION_TEST)
class JDBCEntityRepositoryImplIT extends TestEntityRepositoryImplIT
{
    @SuppressWarnings("unused")
    static Stream<Properties> repositoryConfig()
    {
        return JDBCTestDatabase.repositoryConfigJDBC();
    }

    protected void reset(Properties properties)
    {
        try (JDBCConnection connection = new JDBCConnection(properties) ) {
            connection.tableCommand(JDBCKeyValueRepository.KeyValueSchema.class)
                    .dropTableIfExists(TestAggregate.class)
                    .asIgnore();
        }
    }

}
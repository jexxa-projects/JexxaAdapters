package io.jexxa.common.facade.jdbc;

import io.jexxa.common.facade.TestConstants;
import io.jexxa.common.facade.jdbc.builder.JDBCObject;
import io.jexxa.common.facade.jdbc.builder.SQLDataType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Properties;

import static io.jexxa.common.facade.jdbc.JDBCConnectionPool.getJDBCConnection;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
@Tag(TestConstants.INTEGRATION_TEST)
class JDBCCommandIT
{
    @ParameterizedTest
    @MethodSource(JDBCTestDatabase.JDBC_REPOSITORY_CONFIG)
    void testDeleteValues(Properties properties)
    {
        try (JDBCConnection jdbcConnection = JDBCTestDatabase.setupDatabase(properties))
        {

            //arrange
            var deleteAllRowsQuery = jdbcConnection.command(JDBCTestDatabase.JDBCTestSchema.class)
                    .deleteFrom(JDBCTestDatabase.class)
                    .where(JDBCTestDatabase.JDBCTestSchema.REPOSITORY_KEY).isNotEqual(JDBCTestDatabase.PRIMARY_KEY_WITH_NONNULL_VALUES)
                    .or(JDBCTestDatabase.JDBCTestSchema.REPOSITORY_KEY).isEqual(JDBCTestDatabase.PRIMARY_KEY_WITH_NONNULL_VALUES)
                    .create();

            var validateNoEntriesQuery = jdbcConnection.query(JDBCTestDatabase.JDBCTestSchema.class)
                    .selectAll()
                    .from(JDBCTestDatabase.class)
                    .create();

            //act
            deleteAllRowsQuery.asUpdate();

            //Assert
            assertTrue(validateNoEntriesQuery.isEmpty());
        }
    }

    @ParameterizedTest
    @MethodSource(JDBCTestDatabase.JDBC_REPOSITORY_CONFIG)
    void testUpdateValues(Properties properties)
    {
        try (JDBCConnection jdbcConnection = JDBCTestDatabase.setupDatabase(properties))
        {
            //arrange
            String updatedString = "UpdatesString";

            var updateQuery = jdbcConnection.command(JDBCTestDatabase.JDBCTestSchema.class) //Simulate an equal statement
                    .update(JDBCTestDatabase.class)
                    .set(JDBCTestDatabase.JDBCTestSchema.STRING_TYPE, new JDBCObject( updatedString, SQLDataType.TEXT ))
                    .where(JDBCTestDatabase.JDBCTestSchema.REPOSITORY_KEY).isGreaterOrEqual(JDBCTestDatabase.PRIMARY_KEY_WITH_NONNULL_VALUES)
                    .and(JDBCTestDatabase.JDBCTestSchema.REPOSITORY_KEY).isLessOrEqual(JDBCTestDatabase.PRIMARY_KEY_WITH_NONNULL_VALUES)
                    .create();

            var validateUpdate = jdbcConnection.query(JDBCTestDatabase.JDBCTestSchema.class)
                    .selectAll()
                    .from(JDBCTestDatabase.class)
                    .where(JDBCTestDatabase.JDBCTestSchema.STRING_TYPE).isEqual(updatedString)
                    .create();
            //act
            updateQuery.asUpdate();

            //Assert
            assertEquals(1, validateUpdate.asString().count());
        }
    }


    @ParameterizedTest
    @MethodSource(JDBCTestDatabase.JDBC_REPOSITORY_CONFIG)
    void testCreateDropTable(Properties properties) {
        //Arrange
        var jdbcConnection = getJDBCConnection(properties, JDBCCommandIT.class);
        jdbcConnection.tableCommand(JDBCTestDatabase.JDBCTestSchema.class).dropTableIfExists("TEST_TABLE").asIgnore();

        //Act/Assert
        assertDoesNotThrow( () -> jdbcConnection
                .tableCommand(JDBCTestDatabase.JDBCTestSchema.class)
                .createTable("TEST_TABLE")
                .addColumn(JDBCTestDatabase.JDBCTestSchema.INTEGER_TYPE, SQLDataType.NUMERIC)
                .create()
                .asIgnore()
        );

        assertDoesNotThrow( () -> jdbcConnection
                .tableCommand(JDBCTestDatabase.JDBCTestSchema.class)
                .dropTable("TEST_TABLE")
                .asIgnore()
        );

    }

    @ParameterizedTest
    @MethodSource(JDBCTestDatabase.JDBC_REPOSITORY_CONFIG)
    void testCreateTableOnlyOnce(Properties properties) {
        //Arrange
        var jdbcConnection = getJDBCConnection(properties, JDBCCommandIT.class);
        jdbcConnection.tableCommand(JDBCTestDatabase.JDBCTestSchema.class).dropTableIfExists("TEST_TABLE").asIgnore();

        var createTable = jdbcConnection.tableCommand(JDBCTestDatabase.JDBCTestSchema.class)
                .createTable("TEST_TABLE").addColumn(JDBCTestDatabase.JDBCTestSchema.INTEGER_TYPE, SQLDataType.NUMERIC).create();
        createTable.asIgnore();

        // Act
        assertThrows(IllegalArgumentException.class, createTable::asIgnore);
    }
}

package io.jexxa.adapters.jdbc.facade;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Properties;

import static io.jexxa.adapters.jdbc.facade.JDBCTestDatabase.*;
import static io.jexxa.adapters.jdbc.facade.JDBCTestDatabase.JDBCTestSchema.STRING_TYPE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JDBCSessionIT
{
    @ParameterizedTest
    @MethodSource(JDBC_REPOSITORY_CONFIG)
    void testUpdateAndQueryWithSchema(Properties properties)
    {
        try (JDBCConnection jdbcConnection = setupDatabase(properties))
        {
            // Arrange
            JDBCSession objectUnderTest = new JDBCSession(jdbcConnection);
            String updatedString = "UpdatedString";

            // Act - Command mit Schema
            JDBCCommandBuilder<JDBCTestDatabase.JDBCTestSchema> commandBuilder = objectUnderTest.command(JDBCTestDatabase.JDBCTestSchema.class);
            commandBuilder.update(JDBCTestDatabase.class)
                    .set(STRING_TYPE, new JDBCObject(updatedString, SQLDataType.TEXT))
                    .where(JDBCTestDatabase.JDBCTestSchema.REPOSITORY_KEY).isEqual(PRIMARY_KEY_WITH_NONNULL_VALUES)
                    .create()
                    .asUpdate();

            // Act - Query mit Schema
            JDBCQueryBuilder<JDBCTestDatabase.JDBCTestSchema> queryBuilder = objectUnderTest.query(JDBCTestDatabase.JDBCTestSchema.class);
            var result = queryBuilder.select(STRING_TYPE)
                    .from(JDBCTestDatabase.class)
                    .where(JDBCTestDatabase.JDBCTestSchema.REPOSITORY_KEY).isEqual(PRIMARY_KEY_WITH_NONNULL_VALUES)
                    .create()
                    .asString()
                    .findFirst();

            // Assert
            assertTrue(result.isPresent() && result.get().isPresent());
            assertEquals(updatedString, result.get().get());
        }
    }

    @ParameterizedTest
    @MethodSource(JDBC_REPOSITORY_CONFIG)
    void testCommandWithoutSchema(Properties properties)
    {
        try (JDBCConnection jdbcConnection = setupDatabase(properties))
        {
            // Arrange
            JDBCSession objectUnderTest = new JDBCSession(jdbcConnection);

            // Act
            var commandBuilder = objectUnderTest.command();

            // Assert
            assertNotNull(commandBuilder);
        }
    }

    @ParameterizedTest
    @MethodSource(JDBC_REPOSITORY_CONFIG)
    void testTableCommand(Properties properties)
    {
        try (JDBCConnection jdbcConnection = setupDatabase(properties))
        {
            // Arrange
            JDBCSession objectUnderTest = new JDBCSession(jdbcConnection);

            // Act
            JDBCTableBuilder<JDBCTestSchema> tableBuilder = objectUnderTest.tableCommand(JDBCTestSchema.class);

            // Assert
            assertNotNull(tableBuilder);
            assertDoesNotThrow(() -> tableBuilder.dropTableIfExists(JDBCTestDatabase.class)
                    .asIgnore());
        }
    }

    @ParameterizedTest
    @MethodSource(JDBC_REPOSITORY_CONFIG)
    void testAutocreateDatabase(Properties properties)
    {
        try (JDBCConnection jdbcConnection = setupDatabase(properties))
        {
            // Arrange
            JDBCSession objectUnderTest = new JDBCSession(jdbcConnection);

            // Act & Assert
            assertDoesNotThrow(() -> objectUnderTest.autocreateDatabase(properties));
        }
    }

    @ParameterizedTest
    @MethodSource(JDBC_REPOSITORY_CONFIG)
    void testPreparedStatement(Properties properties)
    {
        try (JDBCConnection jdbcConnection = setupDatabase(properties))
        {
            // Arrange
            JDBCSession objectUnderTest = new JDBCSession(jdbcConnection);
            String sql = "SELECT " + STRING_TYPE.name() + " FROM JDBCTESTDATABASE WHERE "
                    + JDBCTestSchema.REPOSITORY_KEY.name() + " = ?";
            List<Object> arguments = List.of(PRIMARY_KEY_WITH_NONNULL_VALUES);

            // Act
            JDBCQuery query = objectUnderTest.preparedStatement(sql, arguments);
            var result = query.asString().findFirst();

            // Assert
            assertNotNull(query);
            assertTrue(result.isPresent() && result.get().isPresent());
            assertEquals(TEST_STRING, result.get().get());
        }
    }

}
package io.jexxa.adapters.jdbc.facade.database;

import io.jexxa.adapters.jdbc.facade.JDBCConnection;
import io.jexxa.adapters.jdbc.facade.JDBCRepositoryConfig;
import io.jexxa.adapters.jdbc.facade.JDBCTableBuilder.SQLConstraint;
import io.jexxa.adapters.jdbc.facade.SQLDataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Properties;
import java.util.stream.Stream;

import static io.jexxa.adapters.jdbc.facade.JDBCProperties.jdbcUrl;
import static org.junit.jupiter.api.Assertions.*;

class GenericSQLDatabaseTest {

    private static final String SCHEMA_NAME = "generic_sql_db_test";

    enum DummyEntity {
        ID,
        MY_COL,
        OLD_COL,
        NEW_COL,
        TARGET_COL
    }

    static Stream<Properties> jdbcConfigs() {
        Properties h2Props = JDBCRepositoryConfig.h2RepositoryConfig();
        h2Props.put(jdbcUrl(), h2Props.getProperty(jdbcUrl()) + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE");

        return Stream.of(
                JDBCRepositoryConfig.postgresRepositoryConfig(SCHEMA_NAME),
                h2Props
        );
    }

    @ParameterizedTest
    @CsvSource({
            "jdbc:oracle:thin:@localhost:1521:xe, 'VARCHAR(4000) '",
            "jdbc:h2:mem:testdb, VARCHAR",
            "jdbc:mysql://localhost:3306/db, 'VARCHAR(65535) '",
            "jdbc:postgresql://localhost:5432/db, 'VARCHAR(255) '"
    })
    void matchingPrimaryKey_withTextTypes_returnsDialectSpecificMaxVarChar(String url, String expectedSqlType) {
        // Arrange
        GenericSQLDatabase database = new GenericSQLDatabase(url);

        // Act
        SQLDataType fromText = database.matchingPrimaryKey(SQLDataType.TEXT);
        SQLDataType fromVarChar = database.matchingPrimaryKey(SQLDataType.VARCHAR);
        SQLDataType fromJsonb = database.matchingPrimaryKey(SQLDataType.JSONB);

        // Assert
        assertEquals(new SQLDataType(expectedSqlType), fromText);
        assertEquals(new SQLDataType(expectedSqlType), fromVarChar);
        assertEquals(new SQLDataType(expectedSqlType), fromJsonb);
    }

    @Test
    void matchingPrimaryKey_withNonTextType_returnsUnchangedType() {
        // Arrange
        GenericSQLDatabase database = new GenericSQLDatabase("jdbc:h2:mem:testdb");

        // Act
        SQLDataType result = database.matchingPrimaryKey(SQLDataType.INTEGER);

        // Assert
        assertEquals(SQLDataType.INTEGER, result);
    }

    @Test
    void matchingValue_withJsonb_returnsText() {
        // Arrange
        GenericSQLDatabase database = new GenericSQLDatabase("jdbc:h2:mem:testdb");

        // Act
        SQLDataType result = database.matchingValue(SQLDataType.JSONB);

        // Assert
        assertEquals(SQLDataType.TEXT, result);
    }

    @Test
    void matchingValue_withOtherType_returnsUnchangedType() {
        // Arrange
        GenericSQLDatabase database = new GenericSQLDatabase("jdbc:h2:mem:testdb");

        // Act
        SQLDataType result = database.matchingValue(SQLDataType.INTEGER);

        // Assert
        assertEquals(SQLDataType.INTEGER, result);
    }

    @ParameterizedTest
    @MethodSource("jdbcConfigs")
    void columnExist_whenColumnExistsAndWhenMissing_returnsExpectedBoolean(Properties properties) {
        // Arrange
        String url = properties.getProperty(jdbcUrl());
        GenericSQLDatabase database = new GenericSQLDatabase(url);

        try (JDBCConnection connection = new JDBCConnection(properties)) {
            connection.tableCommand().dropTableIfExists(DummyEntity.class).asIgnore();
            connection.tableCommand()
                    .createTable(DummyEntity.class)
                    .addColumn(DummyEntity.ID, SQLDataType.INTEGER).addConstraint(SQLConstraint.PRIMARY_KEY)
                    .addColumn(DummyEntity.MY_COL, SQLDataType.VARCHAR)
                    .create()
                    .asIgnore();

            // Act
            boolean existingColumn = database.columnExist(connection, DummyEntity.class.getSimpleName(), DummyEntity.MY_COL.name());
            boolean missingColumn = database.columnExist(connection, DummyEntity.class.getSimpleName(), "NON_EXISTENT");

            // Assert
            assertTrue(existingColumn);
            assertFalse(missingColumn);
        }
    }


    @ParameterizedTest
    @MethodSource("jdbcConfigs")
    void alterColumnType_changesColumnDataType(Properties properties) {
        // Arrange
        String url = properties.getProperty(jdbcUrl());
        GenericSQLDatabase database = new GenericSQLDatabase(url);

        try (JDBCConnection connection = new JDBCConnection(properties)) {
            connection.tableCommand().dropTableIfExists(DummyEntity.class).asIgnore();
            connection.tableCommand()
                    .createTable(DummyEntity.class)
                    .addColumn(DummyEntity.ID, SQLDataType.INTEGER).addConstraint(SQLConstraint.PRIMARY_KEY)
                    .addColumn(DummyEntity.TARGET_COL, SQLDataType.INTEGER)
                    .create()
                    .asIgnore();

            // Act
            database.alterColumnType(connection, DummyEntity.class, DummyEntity.TARGET_COL.name(), SQLDataType.VARCHAR);

            // Assert
            assertDoesNotThrow(() ->
                    connection.command(DummyEntity.class)
                            .insertInto(DummyEntity.class)
                            .values(new Object[]{1, "NowAString"})
                            .create()
                            .asIgnore()
            );
        }
    }
}

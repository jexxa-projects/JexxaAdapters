package io.jexxa.common.drivenadapter.persistence;



import java.util.Properties;
import java.util.stream.Stream;

import static io.jexxa.common.drivenadapter.persistence.repository.s3.S3KeyValueRepository.S3_ACCESS_KEY;
import static io.jexxa.common.drivenadapter.persistence.repository.s3.S3KeyValueRepository.S3_BUCKET;
import static io.jexxa.common.drivenadapter.persistence.repository.s3.S3KeyValueRepository.S3_ENDPOINT;
import static io.jexxa.common.drivenadapter.persistence.repository.s3.S3KeyValueRepository.S3_SECRET_KEY;
import static io.jexxa.common.facade.jdbc.JDBCProperties.jdbcAutocreateDatabase;
import static io.jexxa.common.facade.jdbc.JDBCProperties.jdbcAutocreateTable;
import static io.jexxa.common.facade.jdbc.JDBCProperties.jdbcDriver;
import static io.jexxa.common.facade.jdbc.JDBCProperties.jdbcPassword;
import static io.jexxa.common.facade.jdbc.JDBCProperties.jdbcTransactionIsolationLevel;
import static io.jexxa.common.facade.jdbc.JDBCProperties.jdbcUrl;
import static io.jexxa.common.facade.jdbc.JDBCProperties.jdbcUsername;


public class RepositoryConfig {
    private static final String USER_NAME = "postgres";
    private static final String USER_PASSWORD = "admin";
    @SuppressWarnings("unused")
    public static Stream<Properties> repositoryConfig(String schemaName) {
        return Stream.of(
                postgresRepositoryConfigSerializable(schemaName),
                postgresRepositoryConfigRepeatableRead(schemaName),
                postgresRepositoryConfig(schemaName),
                h2RepositoryConfig(),
                imdbRepositoryConfig()
        );
    }

    @SuppressWarnings("unused") // Used in ParameterizedTest
    public static Stream<Properties> jdbcRepositoryConfig(String schemaName) {
        return Stream.of(
                postgresRepositoryConfig(schemaName),
                h2RepositoryConfig()
        );
    }

    public static Properties postgresRepositoryConfig(String schemaName) {
        var postgresProperties = new Properties();
        postgresProperties.put(jdbcDriver(), "org.postgresql.Driver");
        postgresProperties.put(jdbcPassword(), USER_PASSWORD);
        postgresProperties.put(jdbcUsername(), USER_NAME);
        postgresProperties.put(jdbcUrl(), "jdbc:postgresql://localhost:5432/" + schemaName);
        postgresProperties.put(jdbcAutocreateTable(), "true");
        postgresProperties.put(jdbcAutocreateDatabase(), "jdbc:postgresql://localhost:5432/postgres");
        return  postgresProperties;
    }

    public static Properties postgresRepositoryConfigSerializable(String schemaName) {
        var postgresProperties = new Properties();
        postgresProperties.put(jdbcDriver(), "org.postgresql.Driver");
        postgresProperties.put(jdbcPassword(), USER_PASSWORD);
        postgresProperties.put(jdbcUsername(), USER_NAME);
        postgresProperties.put(jdbcUrl(), "jdbc:postgresql://localhost:5432/" + schemaName);
        postgresProperties.put(jdbcAutocreateTable(), "true");
        postgresProperties.put(jdbcAutocreateDatabase(), "jdbc:postgresql://localhost:5432/postgres");
        postgresProperties.put(jdbcTransactionIsolationLevel(), "serializable");
        return  postgresProperties;
    }

    public static Properties postgresRepositoryConfigRepeatableRead(String schemaName) {
        var postgresProperties = new Properties();
        postgresProperties.put(jdbcDriver(), "org.postgresql.Driver");
        postgresProperties.put(jdbcPassword(), USER_PASSWORD);
        postgresProperties.put(jdbcUsername(), USER_NAME);
        postgresProperties.put(jdbcUrl(), "jdbc:postgresql://localhost:5432/" + schemaName);
        postgresProperties.put(jdbcAutocreateTable(), "true");
        postgresProperties.put(jdbcAutocreateDatabase(), "jdbc:postgresql://localhost:5432/postgres");
        postgresProperties.put(jdbcTransactionIsolationLevel(), "repeatable-read");
        return  postgresProperties;
    }


    public static Properties h2RepositoryConfig() {
        var h2Properties = new Properties();
        h2Properties.put(jdbcDriver(), "org.h2.Driver");
        h2Properties.put(jdbcPassword(), USER_PASSWORD);
        h2Properties.put(jdbcUsername(), USER_NAME);
        h2Properties.put(jdbcUrl(), "jdbc:h2:mem:jexxa;DB_CLOSE_DELAY=-1");
        h2Properties.put(jdbcAutocreateTable(), "true");

        return h2Properties;
    }
    public static Properties s3RepositoryConfig() {
        var s3Properties = new Properties();
        s3Properties.put(S3_ENDPOINT, "http://localhost:8100");
        s3Properties.put(S3_BUCKET, "jexxa-adapters-test");
        s3Properties.put(S3_SECRET_KEY, "minioadmin");
        s3Properties.put(S3_ACCESS_KEY, "minioadmin");
        return s3Properties;
    }


    public static Properties imdbRepositoryConfig()
    {
        return new Properties();
    }

}

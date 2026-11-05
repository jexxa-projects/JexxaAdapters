package io.jexxa.common.drivenadapter.persistence;



import java.util.Properties;
import java.util.stream.Stream;


public class RepositoryConfig {

    @SuppressWarnings("unused")
    public static Stream<Properties> objectStoreConfig(String schemaName) {
        return Stream.of(
                imdbRepositoryConfig()
        );
    }

    @SuppressWarnings("unused") // Used in ParameterizedTest
    public static Stream<Properties> repositoryConfig(String schemaName) {
        return Stream.of(
                imdbRepositoryConfig()
        );
    }



    public static Properties imdbRepositoryConfig()
    {
        return new Properties();
    }

}

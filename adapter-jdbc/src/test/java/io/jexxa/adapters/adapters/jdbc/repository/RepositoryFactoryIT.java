package io.jexxa.adapters.adapters.jdbc.repository;


import io.jexxa.adapters.api.JexxaContext;
import io.jexxa.adapters.adapters.jdbc.facade.RepositoryConfig;
import io.jexxa.adapters.testapplication.TestEntity;
import io.jexxa.adapters.core.drivenadapter.persistence.RepositoryFactory;
import io.jexxa.adapters.core.drivenadapter.persistence.repository.imdb.IMDBRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Properties;

import static io.jexxa.adapters.testapplication.TestConstants.INTEGRATION_TEST;
import static io.jexxa.adapters.core.drivenadapter.persistence.RepositoryFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Execution(ExecutionMode.SAME_THREAD)
@Tag(INTEGRATION_TEST)
class RepositoryFactoryIT
{


    @BeforeEach
    void init()
    {
        JexxaContext.init();
    }
    @AfterEach
    void cleanup()
    {
        JexxaContext.cleanup();
        RepositoryFactory.defaultSettings();
    }

    @Test
    void validateIMDBFallbackStrategy( )
    {
        //Arrange
        RepositoryFactory.setDefaultRepository(null);

        // Act
        var result =  createRepository(TestEntity.class,
                TestEntity::getKey,
                new Properties());

        //Assert
        assertNotNull(result);
        assertEquals(IMDBRepository.class.getName(), result.getClass().getName() );
    }

    @Test
    void validatePropertiesStrategy( )
    {
        //Arrange
        setDefaultRepository(JDBCKeyValueRepository.class);

        var postgresProperties = RepositoryConfig.postgresRepositoryConfig("jexxa");

        //Act
        var result = createRepository(TestEntity.class,
                TestEntity::getKey,
                postgresProperties);

        //Assert
        assertNotNull(result);
        assertEquals(JDBCKeyValueRepository.class.getName(), result.getClass().getName() );
    }


    @Test
    void validateDefaultStrategyOverProperties()
    {
        //Arrange: Define a JDBC connection in properties but also set a default strategy
        var postgresProperties = RepositoryConfig.postgresRepositoryConfig("jexxa");

        //Act
        setDefaultRepository(IMDBRepository.class);

        var result =  createRepository(TestEntity.class,
                TestEntity::getKey,
                postgresProperties);

        //Assert
        assertNotNull(result);
        assertEquals(IMDBRepository.class.getName(), result.getClass().getName() );
    }

    @Test
    void validateSpecificStrategyOverDefaultStrategy()
    {
        //Arrange: Define a JDBC connection in properties but also set a default strategy
        var postgresProperties = RepositoryConfig.postgresRepositoryConfig("jexxa");

        setDefaultRepository(IMDBRepository.class);  // Set a default strategy which is used in case no specific strategy is defined

        //Act
        setRepository(JDBCKeyValueRepository.class, TestEntity.class );  // Set a specific strategy

        var result =  createRepository(TestEntity.class,
                TestEntity::getKey,
                postgresProperties);

        //Assert
        assertNotNull(result);
        assertEquals(JDBCKeyValueRepository.class.getName(), result.getClass().getName() );
    }
}

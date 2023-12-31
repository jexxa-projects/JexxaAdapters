package io.jexxa.common.drivenadapter.persistence.repository;


import io.jexxa.common.drivenadapter.persistence.RepositoryConfig;
import io.jexxa.common.drivenadapter.persistence.RepositoryManager;
import io.jexxa.common.drivenadapter.persistence.repository.imdb.IMDBRepository;
import io.jexxa.common.drivenadapter.persistence.repository.jdbc.JDBCKeyValueRepository;
import io.jexxa.common.facade.jdbc.TestEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Properties;

import static io.jexxa.common.facade.TestConstants.INTEGRATION_TEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Execution(ExecutionMode.SAME_THREAD)
@Tag(INTEGRATION_TEST)
class RepositoryManagerIT
{

    @AfterEach
    void cleanup()
    {
        RepositoryManager.defaultSettings();
    }

    @Test
    void validateIMDBFallbackStrategy( )
    {
        //Arrange
        RepositoryManager.setDefaultStrategy(null);

        // Act
        var result =  RepositoryManager.getRepository(TestEntity.class,
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
        RepositoryManager.setDefaultStrategy(null);

        var postgresProperties = RepositoryConfig.postgresRepositoryConfig("jexxa");

        //Act
        var result = RepositoryManager.getRepository(TestEntity.class,
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
        RepositoryManager.setDefaultStrategy(IMDBRepository.class);

        var result =  RepositoryManager.getRepository(TestEntity.class,
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

        RepositoryManager.setDefaultStrategy(IMDBRepository.class);  // Set a default strategy which is used in case no specific strategy is defined

        //Act
        RepositoryManager.setStrategy(JDBCKeyValueRepository.class, TestEntity.class );  // Set a specific strategy

        var result =  RepositoryManager.getRepository(TestEntity.class,
                TestEntity::getKey,
                postgresProperties);

        //Assert
        assertNotNull(result);
        assertEquals(JDBCKeyValueRepository.class.getName(), result.getClass().getName() );
    }
}

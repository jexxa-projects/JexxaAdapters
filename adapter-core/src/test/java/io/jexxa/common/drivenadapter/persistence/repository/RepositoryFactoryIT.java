package io.jexxa.common.drivenadapter.persistence.repository;


import io.jexxa.adapterapi.JexxaContext;
import io.jexxa.common.drivenadapter.persistence.RepositoryConfig;
import io.jexxa.common.testapplication.TestEntity;
import io.jexxa.core.drivenadapter.persistence.RepositoryFactory;
import io.jexxa.core.drivenadapter.persistence.repository.imdb.IMDBRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Properties;

import static io.jexxa.common.testapplication.TestConstants.INTEGRATION_TEST;
import static io.jexxa.core.drivenadapter.persistence.RepositoryFactory.createRepository;
import static io.jexxa.core.drivenadapter.persistence.RepositoryFactory.setDefaultRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Execution(ExecutionMode.SAME_THREAD)
@Tag(INTEGRATION_TEST)
class RepositoryFactoryIT {


    @BeforeEach
    void init() {
        JexxaContext.init();
    }

    @AfterEach
    void cleanup() {
        JexxaContext.cleanup();
        RepositoryFactory.defaultSettings();
    }

    @Test
    void validateIMDBFallbackStrategy() {
        //Arrange
        setDefaultRepository(null);

        // Act
        var result = createRepository(TestEntity.class,
                TestEntity::getKey,
                new Properties());

        //Assert
        assertNotNull(result);
        assertEquals(IMDBRepository.class.getName(), result.getClass().getName());
    }


    @Test
    void validateDefaultStrategyOverProperties() {
        //Arrange: Define a JDBC connection in properties but also set a default strategy
        var postgresProperties = RepositoryConfig.s3RepositoryConfig();

        //Act
        setDefaultRepository(IMDBRepository.class);

        var result = createRepository(TestEntity.class,
                TestEntity::getKey,
                postgresProperties);

        //Assert
        assertNotNull(result);
        assertEquals(IMDBRepository.class.getName(), result.getClass().getName());
    }

}
package io.jexxa.common.facade.jdbc;


import io.jexxa.common.facade.TestConstants;
import io.jexxa.common.drivenadapter.persistence.repository.jdbc.JDBCKeyValueRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Properties;

import static io.jexxa.common.facade.jdbc.JDBCProperties.jdbcDriver;
import static io.jexxa.common.facade.jdbc.JDBCProperties.jdbcUrl;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag(TestConstants.UNIT_TEST)
@Execution(ExecutionMode.CONCURRENT)
class JDBCPropertiesTest
{
    @Test
    void emptyProperties()
    {
        //Arrange
        var emptyProperties = new Properties();

        //Act / Assert
        assertThrows(IllegalArgumentException.class, () -> new JDBCKeyValueRepository<>(
                TestEntity.class,
                TestEntity::getKey,
                emptyProperties
        ));
    }

    @Test
    void invalidJDBCDriver()
    {
        //1.Assert missing properties
        var emptyProperties = new Properties();
        assertThrows(IllegalArgumentException.class, () -> new JDBCKeyValueRepository<>(
                TestEntity.class,
                TestEntity::getKey,
                emptyProperties
        ));

        //2.Arrange invalid properties: Invalid Driver
        Properties propertiesInvalidDriver = new Properties();
        propertiesInvalidDriver.put(jdbcDriver(), "org.unknown.Driver");
        propertiesInvalidDriver.put(jdbcUrl(), "jdbc:postgresql://localhost:5432/properties-test");

        //2.Assert invalid properties: Invalid Driver
        assertThrows(IllegalArgumentException.class, () -> new JDBCKeyValueRepository<>(
                TestEntity.class,
                TestEntity::getKey,
                propertiesInvalidDriver
        ));
    }

    @Test
    void invalidJDBCURL()
    {
        //Arrange Invalid JDBC URL
        Properties propertiesInvalidURL = new Properties();
        propertiesInvalidURL.put(jdbcDriver(), "org.postgresql.Driver");
        propertiesInvalidURL.put(jdbcUrl(), "jdbc:unknown://localhost:5432/properties-test");

        //Act / Assert
        assertThrows(IllegalArgumentException.class, () -> new JDBCKeyValueRepository<>(
                TestEntity.class,
                TestEntity::getKey,
                propertiesInvalidURL
        ));
    }
}

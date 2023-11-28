package io.jexxa.common.drivenadapter.persistence.repository;


import io.jexxa.common.drivenadapter.persistence.repository.jdbc.JDBCKeyValueRepository;
import io.jexxa.common.facade.TestConstants;
import io.jexxa.common.facade.jdbc.JDBCConnection;
import io.jexxa.common.facade.jdbc.JDBCTestDatabase;
import io.jexxa.common.facade.testapplication.TestAggregate;
import io.jexxa.common.facade.testapplication.TestValueObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;

import static io.jexxa.common.drivenadapter.persistence.RepositoryManager.getRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Execution(ExecutionMode.SAME_THREAD)
@Tag(TestConstants.INTEGRATION_TEST)
class TestAggregateRepositoryImplIT
{
    private List<TestAggregate> aggregateList;

    @BeforeEach
    void initTests()
    {
       aggregateList= IntStream.range(1, 100)
               .mapToObj(element -> TestAggregate.create(new TestValueObject(element)))
               .toList();
    }

    @ParameterizedTest
    @MethodSource(JDBCTestDatabase.JDBC_REPOSITORY_CONFIG)
    void addAggregate(Properties repositoryProperties)
    {
        //Arrange
        dropTable(repositoryProperties);
        var objectUnderTest = getRepository(TestAggregate.class, TestAggregate::getKey, repositoryProperties);
        objectUnderTest.removeAll();

        //Act
        aggregateList.forEach(objectUnderTest::add);

        //Assert
        assertEquals(aggregateList.size(), objectUnderTest.get().size());
    }


    @ParameterizedTest
    @MethodSource(JDBCTestDatabase.JDBC_REPOSITORY_CONFIG)
    void getAggregateByID(Properties repositoryProperties)
    {
        //Arrange
        dropTable(repositoryProperties);
        var objectUnderTest = getRepository(TestAggregate.class, TestAggregate::getKey, repositoryProperties);
        objectUnderTest.removeAll();
        aggregateList.forEach(objectUnderTest::add);

        //Act
        var resultList = aggregateList.stream()
                .map(aggregate -> objectUnderTest.get(aggregate.getKey()))
                .toList();

        //Assert
        assertEquals(aggregateList.size(), resultList.size());
    }

    @ParameterizedTest
    @MethodSource(JDBCTestDatabase.JDBC_REPOSITORY_CONFIG)
    void removeAggregate(Properties repositoryProperties)
    {
        //Arrange
        dropTable(repositoryProperties);
        var objectUnderTest = getRepository(TestAggregate.class, TestAggregate::getKey, repositoryProperties);
        objectUnderTest.removeAll();
        aggregateList.forEach(objectUnderTest::add);

        //collect elements from Repository using get() which throws a runtime exception in case the element is not available
        var resultList = objectUnderTest.get();

        //Act
        resultList.forEach(element -> objectUnderTest.remove(element .getKey()));

        //Assert
        assertTrue(objectUnderTest.get().isEmpty());
    }


    @ParameterizedTest
    @MethodSource(JDBCTestDatabase.JDBC_REPOSITORY_CONFIG)
    void updateAggregate(Properties repositoryProperties)
    {
        //Arrange
        dropTable(repositoryProperties);
        var objectUnderTest = getRepository(TestAggregate.class, TestAggregate::getKey, repositoryProperties);
        objectUnderTest.removeAll();
        aggregateList.forEach(objectUnderTest::add);

        int aggregateValue = 42;

        //Act
        aggregateList.forEach(element -> element.setInternalValue(aggregateValue));
        aggregateList.forEach(objectUnderTest::update);

        //Assert internal value is correctly set
        objectUnderTest.get().forEach( element -> assertEquals(aggregateValue, element.getInternalValue()) );
    }

    private void dropTable(Properties properties)
    {
        try (JDBCConnection connection = new JDBCConnection(properties) ) {
            connection.createTableCommand(JDBCKeyValueRepository.KeyValueSchema.class)
                    .dropTableIfExists(TestAggregate.class)
                    .asIgnore();
        }
    }
}


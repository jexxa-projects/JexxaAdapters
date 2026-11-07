package io.jexxa.adapters.core.drivenadapter.persistence.repository;


import io.jexxa.adapters.api.JexxaContext;
import io.jexxa.adapters.testapplication.TestAggregate;
import io.jexxa.adapters.testapplication.TestConstants;
import io.jexxa.adapters.testapplication.TestValueObject;
import io.jexxa.adapters.core.drivenadapter.persistence.repository.imdb.IMDBRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.jexxa.adapters.core.drivenadapter.persistence.RepositoryFactory.createRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Execution(ExecutionMode.SAME_THREAD)
@Tag(TestConstants.INTEGRATION_TEST)
public class TestNamedRepositoryIT
{
    private final String REPOSITORY_CONFIG = "repositoryConfig";
    private List<TestAggregate> aggregateList;
    protected static final String TABLE_NAME = "MyNamedRepository";
    @BeforeEach
    void initTests()
    {
       aggregateList= IntStream.range(1, 100)
               .mapToObj(element -> TestAggregate.create(new TestValueObject(element)))
               .toList();

        JexxaContext.init();
    }

    @AfterEach
    void deInit()
    {
        JexxaContext.cleanup();
    }
    @ParameterizedTest
    @MethodSource(REPOSITORY_CONFIG)
    void addAggregate(Properties repositoryProperties)
    {
        //Arrange
        reset(repositoryProperties);
        var objectUnderTest = createRepository(TestAggregate.class, TestAggregate::getKey, TABLE_NAME, repositoryProperties);
        objectUnderTest.removeAll();

        //Act
        aggregateList.forEach(objectUnderTest::add);

        //Assert
        assertEquals(aggregateList.size(), objectUnderTest.get().size());
    }


    @ParameterizedTest
    @MethodSource(REPOSITORY_CONFIG)
    void getAggregateByID(Properties repositoryProperties)
    {
        //Arrange
        reset(repositoryProperties);
        var objectUnderTest = createRepository(TestAggregate.class, TestAggregate::getKey, TABLE_NAME, repositoryProperties);
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
    @MethodSource(REPOSITORY_CONFIG)
    void removeAggregate(Properties repositoryProperties)
    {
        //Arrange
        reset(repositoryProperties);
        var objectUnderTest = createRepository(TestAggregate.class, TestAggregate::getKey, TABLE_NAME, repositoryProperties);
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
    @MethodSource(REPOSITORY_CONFIG)
    void updateAggregate(Properties repositoryProperties)
    {
        //Arrange
        reset(repositoryProperties);
        var objectUnderTest = createRepository(TestAggregate.class, TestAggregate::getKey, TABLE_NAME, repositoryProperties);
        objectUnderTest.removeAll();
        aggregateList.forEach(objectUnderTest::add);

        int aggregateValue = 42;

        //Act
        aggregateList.forEach(element -> element.setInternalValue(aggregateValue));
        aggregateList.forEach(objectUnderTest::update);

        //Assert internal value is correctly set
        objectUnderTest.get().forEach( element -> assertEquals(aggregateValue, element.getInternalValue()) );
    }

    @SuppressWarnings("unused")
    static Stream<Properties> repositoryConfig()
    {
        return Stream.of(new Properties());
    }

    protected void reset(Properties properties)
    {
        IMDBRepository.clear();
    }
}


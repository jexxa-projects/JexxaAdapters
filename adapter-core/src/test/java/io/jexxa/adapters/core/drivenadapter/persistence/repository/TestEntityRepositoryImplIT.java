package io.jexxa.adapters.core.drivenadapter.persistence.repository;

import io.jexxa.adapters.api.JexxaContext;
import io.jexxa.adapters.testapplication.TestConstants;
import io.jexxa.adapters.testapplication.TestEntity;
import io.jexxa.adapters.testapplication.TestValueObject;
import io.jexxa.adapters.core.drivenadapter.persistence.repository.imdb.IMDBRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.jexxa.adapters.core.drivenadapter.persistence.RepositoryFactory.createRepository;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@Tag(TestConstants.INTEGRATION_TEST)
public class TestEntityRepositoryImplIT
{
    private List<TestEntity> aggregateList;
    private static final String ALL_REPOSITORY_CONFIGS = "repositoryConfig";


    @BeforeEach
    void initTests()
    {
        aggregateList = IntStream.range(1, 100)
                .mapToObj(element -> TestEntity.create(new TestValueObject(element)))
                .toList();
        JexxaContext.init();
    }
    @AfterEach
    void deInit()
    {
        JexxaContext.cleanup();
    }
    @ParameterizedTest
    @MethodSource(ALL_REPOSITORY_CONFIGS)
    void addAggregate(Properties repositoryProperties)
    {
        //Arrange
        reset(repositoryProperties);
        var objectUnderTest = createRepository(TestEntity.class, TestEntity::getKey, repositoryProperties);
        objectUnderTest.removeAll();

        //Act
        aggregateList.forEach(objectUnderTest::add);

        //Assert
        assertEquals(aggregateList.size(), objectUnderTest.get().size());
    }

    @ParameterizedTest
    @MethodSource(ALL_REPOSITORY_CONFIGS)
    void testPreconditionAddAggregate(Properties repositoryProperties)
    {
        //Arrange
        reset(repositoryProperties);
        var objectUnderTest = createRepository(TestEntity.class, TestEntity::getKey, repositoryProperties);
        objectUnderTest.removeAll();
        aggregateList.forEach(objectUnderTest::add);
        var firstElement = aggregateList.getFirst();

        //Act / Assert
        assertThrows(IllegalArgumentException.class, () -> objectUnderTest.add(firstElement));
    }


    @ParameterizedTest
    @MethodSource(ALL_REPOSITORY_CONFIGS)
    void getAggregateByID(Properties repositoryProperties)
    {
        //Arrange
        reset(repositoryProperties);
        var objectUnderTest = createRepository(TestEntity.class, TestEntity::getKey, repositoryProperties);
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
    @MethodSource(ALL_REPOSITORY_CONFIGS)
    void removeAggregate(Properties repositoryProperties)
    {
        //Arrange
        reset(repositoryProperties);
        var objectUnderTest = createRepository(TestEntity.class, TestEntity::getKey, repositoryProperties);
        objectUnderTest.removeAll();
        aggregateList.forEach(objectUnderTest::add);

        //collect elements from Repository using get() which throws a runtime exception in case the element is not available
        var resultList = objectUnderTest.get();

        //Act
        resultList.forEach(element -> objectUnderTest.remove(element.getKey()));

        //Assert
        assertTrue(objectUnderTest.get().isEmpty());
    }

    @ParameterizedTest
    @MethodSource(ALL_REPOSITORY_CONFIGS)
    void testPreconditionRemoveAggregate(Properties repositoryProperties)
    {
        //Arrange
        reset(repositoryProperties);
        var objectUnderTest = createRepository(TestEntity.class, TestEntity::getKey, repositoryProperties);
        objectUnderTest.removeAll();

        aggregateList.forEach(objectUnderTest::add);
        var firstElement = aggregateList.getFirst().getKey();

        //Act / Assert
        objectUnderTest.removeAll();
        assertThrows(IllegalArgumentException.class, () -> objectUnderTest.remove(firstElement));
    }


    @ParameterizedTest
    @MethodSource(ALL_REPOSITORY_CONFIGS)
    void updateAggregate(Properties repositoryProperties)
    {
        //Arrange
        reset(repositoryProperties);
        var objectUnderTest = createRepository(TestEntity.class, TestEntity::getKey, repositoryProperties);
        objectUnderTest.removeAll();
        aggregateList.forEach(objectUnderTest::add);

        int aggregateValue = 42;

        //Act
        aggregateList.forEach(element -> element.setInternalValue(aggregateValue));
        aggregateList.forEach(objectUnderTest::update);

        //Assert internal value is correctly set
        objectUnderTest.get().forEach(element -> assertEquals(aggregateValue, element.getInternalValue()));
    }

    @ParameterizedTest
    @MethodSource(ALL_REPOSITORY_CONFIGS)
    void changeAggregateOnlyAfterUpdate(Properties repositoryProperties)
    {
        //Arrange
        reset(repositoryProperties);
        var objectUnderTest = createRepository(TestEntity.class, TestEntity::getKey, repositoryProperties);
        objectUnderTest.removeAll();
        aggregateList.forEach(objectUnderTest::add);

        int aggregateValue = 42;
        aggregateList.forEach(element -> element.setInternalValue(aggregateValue));

        //Act
        var result = objectUnderTest.get();

        //Assert internal value is correctly set
        result.forEach( element -> assertEquals(0, element.getInternalValue()) );
    }

    @SuppressWarnings("unused")
    Stream<Properties> repositoryConfig()
    {
        return Stream.of( new Properties() );
    }

    @SuppressWarnings("java:S1172")
    protected void reset(Properties properties)
    {
        IMDBRepository.clear();
    }

}
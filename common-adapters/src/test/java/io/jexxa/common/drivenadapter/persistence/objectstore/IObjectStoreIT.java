package io.jexxa.common.drivenadapter.persistence.objectstore;


import io.jexxa.adapterapi.JexxaContext;
import io.jexxa.common.drivenadapter.persistence.objectstore.metadata.MetaTag;
import io.jexxa.common.drivenadapter.persistence.objectstore.metadata.MetaTags;
import io.jexxa.common.drivenadapter.persistence.objectstore.metadata.MetadataSchema;
import io.jexxa.common.facade.TestConstants;
import io.jexxa.common.facade.jdbc.JDBCConnection;
import io.jexxa.common.facade.testapplication.TestValueObject;
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

import static io.jexxa.common.drivenadapter.persistence.ObjectStoreFactory.createObjectStore;
import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
@Tag(TestConstants.INTEGRATION_TEST)
class IObjectStoreIT
{
    private static final int TEST_DATA_SIZE = 100;

    private List<TestObject> testData;
    private IObjectStore<TestObject, TestValueObject, TestObjectSchema> objectUnderTest;

    @BeforeEach
    void initTest()
    {
        testData = IntStream.range(0, TEST_DATA_SIZE)
                .mapToObj(element -> TestObject.create(new TestValueObject(element)))
                .toList();

        testData.forEach(element -> element.setInternalValue(element.getKey().getValue()));
        JexxaContext.init();
    }

    @AfterEach
    void deInit()
    {
        JexxaContext.cleanup();
    }

    /**
     * Defines the metadata that we use:
     * Conventions for databases:
     * - Enum name is used for the name of the row so that there is a direct mapping between the strategy and the database
     * - Adding a new strategy in code after initial usage requires that the database is extended in some woy
     */
    private enum TestObjectSchema implements MetadataSchema
    {
        INT_VALUE(MetaTags.numericTag(TestObject::getInternalValue)),

        VALUE_OBJECT(MetaTags.numericTag(TestObject::getKey, TestValueObject::getValue));

        /**
         *  Defines the constructor of the enum. Following code is equal for all object stores.
         */
        private final MetaTag<TestObject, ?, ? > metaTag;

        TestObjectSchema(MetaTag<TestObject,?, ?> metaTag)
        {
            this.metaTag = metaTag;
        }

        @Override
        @SuppressWarnings("unchecked")
        public MetaTag<TestObject, ?, ?> getTag()
        {
            return metaTag;
        }
    }

    @ParameterizedTest
    @MethodSource(ObjectStoreTestDatabase.REPOSITORY_CONFIG)
    void testAdd(Properties properties)
    {
        //Arrange
        initObjectStore(properties);
        objectUnderTest.removeAll();

        //Act
        testData.forEach(objectUnderTest::add);

        //Assert
        assertEquals(TEST_DATA_SIZE, objectUnderTest.get().size());
    }

    @ParameterizedTest
    @MethodSource(ObjectStoreTestDatabase.REPOSITORY_CONFIG)
    void testRemoveAll(Properties properties)
    {
        //Arrange
        initObjectStore(properties);

        //Act
        objectUnderTest.removeAll();

        //Assert
        assertEquals(0, objectUnderTest.get().size());
    }

    @ParameterizedTest
    @MethodSource(ObjectStoreTestDatabase.REPOSITORY_CONFIG)
    void testRemove(Properties properties)
    {
        //Arrange
        initObjectStore(properties);

        var elementToRemove = testData.stream()
                .findFirst()
                .orElseThrow()
                .getKey();

        //Act
        objectUnderTest.remove(elementToRemove);

        //Assert
        assertTrue(objectUnderTest.get(elementToRemove).isEmpty());
        assertEquals(TEST_DATA_SIZE - 1 , objectUnderTest.get().size());
    }

    @ParameterizedTest
    @MethodSource(ObjectStoreTestDatabase.REPOSITORY_CONFIG)
    void testGetAll(Properties properties)
    {
        //Arrange
        initObjectStore(properties);

        //Act
        var result = objectUnderTest.get();
        result = result.stream()
                .sorted( comparing(element -> element.getKey().getValue()))
                .toList();

        //Assert
        assertEquals(testData, result);
    }

    @ParameterizedTest
    @MethodSource(ObjectStoreTestDatabase.REPOSITORY_CONFIG)
    void testGet(Properties properties)
    {
        //Arrange
        initObjectStore(properties);

        //Act
        var result = objectUnderTest.get(testData.get(0).getKey());

        //Assert
        assertTrue(result.isPresent());
    }

    @ParameterizedTest
    @MethodSource(ObjectStoreTestDatabase.REPOSITORY_CONFIG)
    void testUpdate(Properties properties)
    {
        //Arrange
        initObjectStore(properties);

        //Act
        testData.forEach(element -> element.setInternalValue(TEST_DATA_SIZE));
        testData.forEach(objectUnderTest::update);
        var result = objectUnderTest.get();

        //Assert
        assertTrue(result.stream().allMatch(element -> element.getInternalValue() == TEST_DATA_SIZE));
    }

    void initObjectStore(Properties properties)
    {
        if (!properties.isEmpty())
        {
            try(JDBCConnection jdbcConnection = new JDBCConnection(properties))
            {
                jdbcConnection.tableCommand(TestObjectSchema.class)
                        .dropTableIfExists(TestObject.class)
                        .asIgnore();
            }
        }

        objectUnderTest = createObjectStore(
                TestObject.class,
                TestObject::getKey,
                TestObjectSchema.class,
                properties);

        objectUnderTest.removeAll();

        testData.forEach(element -> element.setInternalValue(element.getKey().getValue()));
        testData.forEach(objectUnderTest::add);
    }
}

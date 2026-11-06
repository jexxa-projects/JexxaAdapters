package io.jexxa.common.drivenadapter.persistence.objectstore;


import io.jexxa.adapterapi.JexxaContext;
import io.jexxa.core.drivenadapter.persistence.objectstore.IObjectStore;
import io.jexxa.core.drivenadapter.persistence.objectstore.imdb.IMDBObjectStore;
import io.jexxa.core.drivenadapter.persistence.objectstore.metadata.MetaTag;
import io.jexxa.core.drivenadapter.persistence.objectstore.metadata.MetaTags;
import io.jexxa.core.drivenadapter.persistence.objectstore.metadata.MetadataSchema;
import io.jexxa.common.testapplication.TestConstants;
import io.jexxa.common.testapplication.TestValueObject;
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

import static io.jexxa.core.drivenadapter.persistence.ObjectStoreFactory.createObjectStore;
import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
@Tag(TestConstants.INTEGRATION_TEST)
public abstract class INamedObjectStoreIT
{
    private static final int TEST_DATA_SIZE = 100;
    protected static final String TABLE_NAME = "MyNamedObjectStore";
    private static final String OBJECT_STORE_CONFIG = "objectStoreConfig";

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
    protected enum TestObjectSchema implements MetadataSchema
    {
        INT_VALUE(MetaTags.numericTag(TestObject::getInternalValue)),

        VALUE_OBJECT(MetaTags.numericTag(TestObject::getKey, TestValueObject::getValue));

        /**
         *  Defines the constructor of the enum. The following code is equal for all object stores.
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
    @MethodSource(OBJECT_STORE_CONFIG)
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
    @MethodSource(OBJECT_STORE_CONFIG)
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
    @MethodSource(OBJECT_STORE_CONFIG)
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
    @MethodSource(OBJECT_STORE_CONFIG)
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
    @MethodSource(OBJECT_STORE_CONFIG)
    void testGet(Properties properties)
    {
        //Arrange
        initObjectStore(properties);

        //Act
        var result = objectUnderTest.get(testData.getFirst().getKey());

        //Assert
        assertTrue(result.isPresent());
    }

    @ParameterizedTest
    @MethodSource(OBJECT_STORE_CONFIG)
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

    protected void setObjectStore(IObjectStore<TestObject, TestValueObject, TestObjectSchema> objectUnderTest)
    {
        this.objectUnderTest = objectUnderTest;
        testData.forEach(element -> element.setInternalValue(element.getKey().getValue()));
        testData.forEach(objectUnderTest::add);
    }

    protected void initObjectStore(Properties properties)
    {
        reset(properties);
        objectUnderTest = createObjectStore(
                TestObject.class,
                TestObject::getKey,
                TABLE_NAME,
                TestObjectSchema.class,
                properties);

        objectUnderTest.removeAll();

        setObjectStore(objectUnderTest);
    }

    protected void reset(Properties properties) {
        IMDBObjectStore.clear();
    }

    @SuppressWarnings("unused")
    private static Stream<Properties> objectStoreConfig() {
        return Stream.of( new Properties() );
    }
}

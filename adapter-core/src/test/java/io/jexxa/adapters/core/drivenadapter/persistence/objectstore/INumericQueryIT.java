package io.jexxa.adapters.core.drivenadapter.persistence.objectstore;


import io.jexxa.adapters.api.JexxaContext;
import io.jexxa.adapters.core.drivenadapter.persistence.objectstore.imdb.IMDBObjectStore;
import io.jexxa.adapters.core.drivenadapter.persistence.objectstore.metadata.MetaTag;
import io.jexxa.adapters.core.drivenadapter.persistence.objectstore.metadata.MetadataSchema;
import io.jexxa.adapters.testapplication.TestConstants;
import io.jexxa.adapters.testapplication.TestValueObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.jexxa.adapters.core.drivenadapter.persistence.ObjectStoreFactory.createObjectStore;
import static io.jexxa.adapters.core.drivenadapter.persistence.objectstore.metadata.MetaTags.numericTag;
import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@Tag(TestConstants.INTEGRATION_TEST)
public class INumericQueryIT
{
    private static final int TEST_DATA_SIZE = 100;
    private static final String OBJECT_STORE_CONFIG = "objectStoreConfig";

    private List<TestObject> testData;
    private IObjectStore<TestObject, TestValueObject, TestObjectSchema> objectStore;

    /**
     * Defines the metadata that we use:
     * Conventions for databases:
     * - Enum name is used for the name of the row so that there is a direct mapping between the strategy and the database
     * - Adding a new strategy in code after initial usage requires that the database is extended in some woy
     */
    protected enum TestObjectSchema implements MetadataSchema
    {
        INT_VALUE(numericTag(TestObject::getInternalValue)),

        VALUE_OBJECT(numericTag(TestObject::getKey, TestValueObject::getValue)),

        OPTIONAL_VALUE_OBJECT(numericTag(TestObject::getOptionalValue, TestValueObject::getValue));

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

    @BeforeEach
    void initTestData()
    {
        testData = IntStream.range(0, TEST_DATA_SIZE)
                .mapToObj(element -> TestObject.create(new TestValueObject(element)))
                .toList();

        testData.forEach(element -> element.setInternalValue(element.getKey().getValue())); // set an internal int value to an ascending number

        testData.stream().limit(50).forEach( element -> element.setOptionalValue( element.getKey() )); // Set the optional value to half of the test data (0 to 49)
        JexxaContext.init();
    }
    @AfterEach
    void deInit()
    {
        JexxaContext.cleanup();
    }


    @ParameterizedTest
    @MethodSource(OBJECT_STORE_CONFIG)
    void testInequalityOperatorsIntValue(Properties properties)
    {
        //Arrange
        initObjectStore(properties);
        var objectUnderTest = objectStore.getNumericQuery( TestObjectSchema.INT_VALUE, Integer.class);

        var greaterOrEqualThanExpected = IntStream.range(50,100)
                .mapToObj(element -> TestObject.create(new TestValueObject(element)))
                .toList();
        var lessOrEqualThanThanExpected = IntStream.rangeClosed(0,50)
                .mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();
        var greaterThanExpected = IntStream.range(51, 100)
                .mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();
        var lessThanExpected = IntStream.range(0,50).
                mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();

        //Act
        var greaterOrEqualThan = objectUnderTest.isGreaterOrEqualThan(50);
        var lessOrEqualThan = objectUnderTest.isLessOrEqualThan(50);
        var greaterThan = objectUnderTest.isGreaterThan(50);
        var lessThan = objectUnderTest.isLessThan(50);


        //Assert
        assertEquals(greaterOrEqualThanExpected, greaterOrEqualThan);
        assertEquals(greaterThanExpected, greaterThan);

        assertEquals(lessThanExpected, lessThan);
        assertEquals(lessOrEqualThanThanExpected, lessOrEqualThan);
    }

    @ParameterizedTest
    @MethodSource(OBJECT_STORE_CONFIG)
    void testEqualityOperatorsIntValue(Properties properties)
    {
        //Arrange
        initObjectStore(properties);
        var objectUnderTest = objectStore.getNumericQuery( TestObjectSchema.INT_VALUE, Integer.class);

        var equalToExpected = IntStream.rangeClosed(0,0).
                mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();
        var notEqualToExpected = IntStream.range(1,100).
                mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();

        //Act
        var equalTo = objectUnderTest.isEqualTo(0);
        var notEqualTo = objectUnderTest.isNotEqualTo(0);

        //Assert
        assertEquals(new HashSet<>(notEqualToExpected), new HashSet<>(notEqualTo));
        assertEquals(equalToExpected, equalTo);
    }

    @ParameterizedTest
    @MethodSource(OBJECT_STORE_CONFIG)
    void testRangeOperatorsIntValue(Properties properties)
    {
        //Arrange
        initObjectStore(properties);
        var objectUnderTest = objectStore. getNumericQuery( TestObjectSchema.INT_VALUE, Integer.class);

        var rangeClosedExpected = IntStream.rangeClosed(30,50).
                mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();
        var rangeExpected = IntStream.range(30,50).
                mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();

        //Act
        var rangeClosed = objectUnderTest.getRangeClosed(30,50);
        var range = objectUnderTest.getRange(30,50);

        //Assert
        assertEquals(rangeClosedExpected, rangeClosed);
        assertEquals(rangeExpected, range);
    }

    @ParameterizedTest
    @MethodSource(OBJECT_STORE_CONFIG)
    void testInequalityOperatorValueObject(Properties properties)
    {
        //Arrange
        initObjectStore(properties);

        var objectUnderTest = objectStore.getNumericQuery( TestObjectSchema.VALUE_OBJECT, TestValueObject.class);

        var greaterOrEqualThanExpected = IntStream
                .range(50,100)
                .mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();
        var lessOrEqualThanThanExpected = IntStream.rangeClosed(0,50)
                .mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();
        var greaterThanExpected = IntStream.range(51, 100)
                .mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();
        var lessThanExpected = IntStream.range(0,50)
                .mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();

        //Act
        var greaterOrEqualThan = objectUnderTest.isGreaterOrEqualThan(new TestValueObject(50));
        var lessOrEqualThan = objectUnderTest.isLessOrEqualThan(new TestValueObject(50));
        var greaterThan = objectUnderTest.isGreaterThan(new TestValueObject(50));
        var lessThan = objectUnderTest.isLessThan(new TestValueObject(50));

        //Assert
        assertEquals(greaterOrEqualThanExpected, greaterOrEqualThan);
        assertEquals(greaterThanExpected, greaterThan);

        assertEquals(lessThanExpected, lessThan);
        assertEquals(lessOrEqualThanThanExpected, lessOrEqualThan);
    }

    @ParameterizedTest
    @MethodSource(OBJECT_STORE_CONFIG)
    void testEqualityOperatorValueObject(Properties properties)
    {
        //Arrange
        initObjectStore(properties);

        var objectUnderTest = objectStore.getNumericQuery( TestObjectSchema.VALUE_OBJECT, TestValueObject.class);

        var equalToExpected = IntStream.rangeClosed(0,0).
                mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();
        var notEqualToExpected = IntStream.range(1,100).
                mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();

        //Act
        var equalTo = objectUnderTest.isEqualTo(new TestValueObject(0));
        var notEqualTo = objectUnderTest.isNotEqualTo(new TestValueObject(0));

        //Assert
        assertEquals(notEqualToExpected, notEqualTo);
        assertEquals(equalToExpected, equalTo);
    }

    @ParameterizedTest
    @MethodSource(OBJECT_STORE_CONFIG)
    void testRangeOperatorValueObject(Properties properties)
    {
        //Arrange
        initObjectStore(properties);

        var objectUnderTest = objectStore.getNumericQuery( TestObjectSchema.VALUE_OBJECT, TestValueObject.class);

        var rangeClosedExpected = IntStream.rangeClosed(30,50)
                .mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();
        var rangeExpected = IntStream.range(30,50)
                .mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();

        //Act
        var rangeClosed = objectUnderTest.getRangeClosed(new TestValueObject(30),new TestValueObject(50));
        var range = objectUnderTest.getRange(new TestValueObject(30),new TestValueObject(50));

        //Assert
        assertEquals(rangeClosedExpected, rangeClosed);
        assertEquals(rangeExpected, range);
    }

    @ParameterizedTest
    @MethodSource(OBJECT_STORE_CONFIG)
    void testInequalityOperatorOptionalValueObject(Properties properties)
    {
        //Arrange
        initObjectStore(properties);

        var objectUnderTest = objectStore.getNumericQuery( TestObjectSchema.OPTIONAL_VALUE_OBJECT, TestValueObject.class);

        var lessOrEqualThanThanExpected = IntStream.rangeClosed(0,49)
                .mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();
        var lessThanExpected = IntStream.rangeClosed(0,49)
                .mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();

        //Act
        var greaterOrEqualThan = objectUnderTest.isGreaterOrEqualThan(new TestValueObject(50));
        var lessOrEqualThan = objectUnderTest.isLessOrEqualThan(new TestValueObject(50));
        var greaterThan = objectUnderTest.isGreaterThan(new TestValueObject(50));
        var lessThan = objectUnderTest.isLessThan(new TestValueObject(50));

        //Assert
        assertEquals(Collections.emptyList(), greaterOrEqualThan);
        assertEquals(Collections.emptyList(), greaterThan);

        assertEquals(lessThanExpected, lessThan);
        assertEquals(lessOrEqualThanThanExpected, lessOrEqualThan);
    }

    @ParameterizedTest
    @MethodSource(OBJECT_STORE_CONFIG)
    void testEqualityOperatorOptionalValueObject(Properties properties)
    {
        //Arrange
        initObjectStore(properties);

        var objectUnderTest = objectStore.getNumericQuery( TestObjectSchema.OPTIONAL_VALUE_OBJECT, TestValueObject.class);

        var isNotNullExpected = IntStream.rangeClosed(0,49)
                .mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();
        var isNullExpected = IntStream.range(50,100)
                .mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();

        //Act
        var isNull = objectUnderTest.isNull();
        var isNotNull = objectUnderTest.isNotNull();

        //Assert
        assertEquals(isNullExpected, isNull);
        assertEquals(isNotNullExpected, isNotNull);
    }

    @ParameterizedTest
    @MethodSource(OBJECT_STORE_CONFIG)
    void testRangeOperatorOptionalValueObject(Properties properties)
    {
        //Arrange
        initObjectStore(properties);

        var objectUnderTest = objectStore.getNumericQuery( TestObjectSchema.OPTIONAL_VALUE_OBJECT, TestValueObject.class);

        var rangeClosedExpected = IntStream.rangeClosed(30,49)
                .mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();
        var rangeExpected = IntStream.rangeClosed(30,49)
                .mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();

        //Act
        var rangeClosed = objectUnderTest.getRangeClosed(new TestValueObject(30),new TestValueObject(50));
        var range = objectUnderTest.getRange(new TestValueObject(30),new TestValueObject(50));

        //Assert
        assertEquals(rangeClosedExpected, rangeClosed);
        assertEquals(rangeExpected, range);
    }

    @ParameterizedTest
    @MethodSource(OBJECT_STORE_CONFIG)
    void testGetAscending(Properties properties)
    {
        //Arrange
        initObjectStore(properties);

        var objectUnderTest = objectStore.getNumericQuery( TestObjectSchema.INT_VALUE, Integer.class);
        var expectedResult = testData.stream()
                .sorted(comparing( TestObject::getInternalValue))
                .toList();

        //Act
        var result = objectUnderTest.getAscending();

        //Assert
        assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @MethodSource(OBJECT_STORE_CONFIG)
    void testGetAscendingWithOptionalValue(Properties properties)
    {
        //Arrange
        initObjectStore(properties);

        var objectUnderTest = objectStore.getNumericQuery( TestObjectSchema.OPTIONAL_VALUE_OBJECT, TestValueObject.class);

        var expectedResult = testData.stream()
                .sorted(comparing( TestObject::getInternalValue))
                .toList();

        //Act
        var result = objectUnderTest.getAscending();

        //Assert
        assertEquals(expectedResult.size(), result.size());
        //We can only compare the order of values without a null because there is no additional rule on how to order NULLs
        assertEquals(expectedResult.stream().limit(50).toList(), result.stream().limit(50).toList());
    }

    @ParameterizedTest
    @MethodSource(OBJECT_STORE_CONFIG)
    void testGetDescendingWithOptionalValue(Properties properties)
    {
        //Arrange
        initObjectStore(properties);

        var objectUnderTest = objectStore.getNumericQuery( TestObjectSchema.OPTIONAL_VALUE_OBJECT, TestValueObject.class);

        var expectedResult = Stream.concat(
                testData.stream().limit(50).sorted(comparing(TestObject::getInternalValue).reversed()),
                testData.stream().skip(50))
                .toList();

        //Act
        var result = objectUnderTest.getDescending();

        //Assert
        assertEquals(expectedResult.size(), result.size());
        //We can only compare the order of values without a null because there is no additional rule on how to order NULLs
        assertEquals(expectedResult.stream().limit(50).toList(), result.stream().limit(50).toList());
    }

    @ParameterizedTest
    @MethodSource(OBJECT_STORE_CONFIG)
    void testGetAscendingWithLimit(Properties properties)
    {
        //Arrange
        initObjectStore(properties);

        var objectUnderTest = objectStore.getNumericQuery( TestObjectSchema.INT_VALUE, Integer.class);
        var limitAmount = 10 ;
        var expectedResult = testData.stream()
                .sorted(comparing( TestObject::getInternalValue))
                .limit(limitAmount).toList();

        //Act
        var result = objectUnderTest.getAscending(limitAmount);

        //Assert
        assertEquals(limitAmount, result.size());
        assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @MethodSource(OBJECT_STORE_CONFIG)
    void testGetDescending(Properties properties)
    {
        //Arrange
        initObjectStore(properties);

        var objectUnderTest = objectStore.getNumericQuery( TestObjectSchema.INT_VALUE, Integer.class);
        var expectedResult = testData.stream()
                .sorted(comparing( TestObject::getInternalValue).reversed())
                .toList();

        //Act
        var result = objectUnderTest.getDescending();

        //Assert
        assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @MethodSource(OBJECT_STORE_CONFIG)
    void testGetDescendingWithLimit(Properties properties)
    {
        //Arrange
        initObjectStore(properties);

        var objectUnderTest = objectStore.getNumericQuery( TestObjectSchema.INT_VALUE, Integer.class);
        var limitAmount = 10 ;
        var expectedResult = testData.stream()
                .sorted(comparing( TestObject::getInternalValue).reversed())
                .limit(limitAmount).toList();

        //Act
        var result = objectUnderTest.getDescending(limitAmount);

        //Assert
        assertEquals(limitAmount, result.size());
        assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @MethodSource(OBJECT_STORE_CONFIG)
    void testQueryUsesLatestData(Properties properties) {
        //Arrange
        initObjectStore(properties);
        objectStore.removeAll();

        //Act
        var objectUnderTest = objectStore.getNumericQuery(TestObjectSchema.INT_VALUE, Integer.class);
        testData.forEach(objectStore::add);
        var result = objectUnderTest.getAscending();

        //Assert
        assertEquals(testData.size(), result.size());
    }

    protected void setObjectStore(IObjectStore<TestObject, TestValueObject, TestObjectSchema> objectStore) {
        this.objectStore = objectStore;
        testData.forEach(objectStore::add);
    }
    protected void initObjectStore(Properties properties)
    {
        reset(properties);

        objectStore = createObjectStore(
                TestObject.class,
                TestObject::getKey,
                TestObjectSchema.class,
                properties);

        objectStore.removeAll();

        setObjectStore(objectStore);
    }

    @SuppressWarnings("java:S1172")
    protected void reset(Properties properties)
    {
        IMDBObjectStore.clear();
    }
    @SuppressWarnings("unused")
    Stream<Properties> objectStoreConfig() {
        return Stream.of( new Properties() );
    }
}

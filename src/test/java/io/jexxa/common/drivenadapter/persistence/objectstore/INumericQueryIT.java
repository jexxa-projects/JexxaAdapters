package io.jexxa.common.drivenadapter.persistence.objectstore;


import io.jexxa.common.drivenadapter.persistence.ObjectStoreManager;
import io.jexxa.common.drivenadapter.persistence.objectstore.metadata.MetaTag;
import io.jexxa.common.drivenadapter.persistence.objectstore.metadata.MetadataSchema;
import io.jexxa.common.facade.jdbc.JDBCConnection;
import io.jexxa.common.facade.testapplication.TestValueObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.jexxa.common.drivenadapter.persistence.objectstore.ObjectStoreTestDatabase.REPOSITORY_CONFIG;
import static io.jexxa.common.drivenadapter.persistence.objectstore.metadata.MetaTags.numericTag;
import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.assertEquals;

class INumericQueryIT
{
    private static final int TEST_DATA_SIZE = 100;

    private List<TestObject> testData;
    private IObjectStore<TestObject, TestValueObject, TestObjectSchema> objectStore;

    /**
     * Defines the metadata that we use:
     * Conventions for databases:
     * - Enum name is used for the name of the row so that there is a direct mapping between the strategy and the database
     * - Adding a new strategy in code after initial usage requires that the database is extended in some woy
     */
    private enum TestObjectSchema implements MetadataSchema
    {
        INT_VALUE(numericTag(TestObject::getInternalValue)),

        VALUE_OBJECT(numericTag(TestObject::getKey, TestValueObject::getValue)),

        OPTIONAL_VALUE_OBJECT(numericTag(TestObject::getOptionalValue, TestValueObject::getValue));

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

    @BeforeEach
    void initTestData()
    {
        testData = IntStream.range(0, TEST_DATA_SIZE)
                .mapToObj(element -> TestObject.create(new TestValueObject(element)))
                .toList();

        testData.forEach(element -> element.setInternalValue(element.getKey().getValue())); // set internal int value to an ascending number

        testData.stream().limit(50).forEach( element -> element.setOptionalValue( element.getKey() )); // Set optional value to half ot the test data (0 to 49)
    }


    @ParameterizedTest
    @MethodSource(REPOSITORY_CONFIG)
    void testInequalityOperatorsIntValue(Properties properties)
    {
        //Arrange
        initObjectStore(properties);
        var objectUnderTest = objectStore. getNumericQuery( TestObjectSchema.INT_VALUE, Integer.class);

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
    @MethodSource(REPOSITORY_CONFIG)
    void testEqualityOperatorsIntValue(Properties properties)
    {
        //Arrange
        initObjectStore(properties);
        var objectUnderTest = objectStore. getNumericQuery( TestObjectSchema.INT_VALUE, Integer.class);

        var equalToExpected = IntStream.rangeClosed(0,0).
                mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();
        var notEqualToExpected = IntStream.range(1,100).
                mapToObj(element -> TestObject.create(new TestValueObject(element))).toList();

        //Act
        var equalTo = objectUnderTest.isEqualTo(0);
        var notEqualTo = objectUnderTest.isNotEqualTo(0);

        //Assert
        assertEquals(notEqualToExpected, notEqualTo);
        assertEquals(equalToExpected, equalTo);
    }

    @ParameterizedTest
    @MethodSource(REPOSITORY_CONFIG)
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
    @MethodSource(REPOSITORY_CONFIG)
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
    @MethodSource(REPOSITORY_CONFIG)
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
    @MethodSource(REPOSITORY_CONFIG)
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
    @MethodSource(REPOSITORY_CONFIG)
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
    @MethodSource(REPOSITORY_CONFIG)
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
    @MethodSource(REPOSITORY_CONFIG)
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
    @MethodSource(REPOSITORY_CONFIG)
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
    @MethodSource(REPOSITORY_CONFIG)
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
        assertEquals(expectedResult.stream().limit(50).toList(), result.stream().limit(50).toList());//We can only compare the order of values without null because there is no additional rule how to order NULLs
    }

    @ParameterizedTest
    @MethodSource(REPOSITORY_CONFIG)
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
        assertEquals(expectedResult.stream().limit(50).toList(), result.stream().limit(50).toList()); //We can only compare the order of values without null because there is no additional rule how to order NULLs
    }

    @ParameterizedTest
    @MethodSource(REPOSITORY_CONFIG)
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
    @MethodSource(REPOSITORY_CONFIG)
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
    @MethodSource(REPOSITORY_CONFIG)
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
    @MethodSource(REPOSITORY_CONFIG)
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
    void initObjectStore(Properties properties)
    {
        if (!properties.isEmpty()) {
            try (JDBCConnection jdbcConnection = new JDBCConnection(properties)) {
                jdbcConnection.createTableCommand(TestObjectSchema.class)
                        .dropTableIfExists(TestObject.class)
                        .asIgnore();

            }
        }

        objectStore = ObjectStoreManager.getObjectStore(
                TestObject.class,
                TestObject::getKey,
                TestObjectSchema.class,
                properties);

        objectStore.removeAll();

        testData.forEach(objectStore::add);
    }
}

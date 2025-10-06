package io.jexxa.common.drivenadapter.persistence.objectstore;


import io.jexxa.adapterapi.JexxaContext;
import io.jexxa.common.drivenadapter.persistence.objectstore.metadata.MetaTag;
import io.jexxa.common.drivenadapter.persistence.objectstore.metadata.MetadataSchema;
import io.jexxa.common.facade.jdbc.JDBCConnection;
import io.jexxa.common.facade.testapplication.TestValueObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;

import static io.jexxa.common.drivenadapter.persistence.ObjectStoreFactory.createObjectStore;
import static io.jexxa.common.drivenadapter.persistence.objectstore.ObjectStoreTestDatabase.REPOSITORY_CONFIG;
import static io.jexxa.common.drivenadapter.persistence.objectstore.TestObject.createCharSequence;
import static io.jexxa.common.drivenadapter.persistence.objectstore.metadata.MetaTags.numericTag;
import static io.jexxa.common.drivenadapter.persistence.objectstore.metadata.MetaTags.stringTag;
import static io.jexxa.common.facade.jdbc.JDBCProperties.jdbcUrl;
import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IStringQueryIT
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

        OPTIONAL_VALUE_OBJECT(numericTag(TestObject::getOptionalValue, TestValueObject::getValue)),

        STRING_OBJECT(stringTag(TestObject::getString)),

        OPTIONAL_STRING_OBJECT(stringTag(TestObject::getOptionalString));

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

        // set internal int value to an ascending number
        // the internal string is set to A, B, ..., AA, AB, ...
        testData.forEach(element -> element.setInternalValue(element.getKey().getValue()));

        // Set an optional string in the first 50 elements to A, B, ..., AA, AB, ...
        testData.stream().limit(50).forEach(element -> element.setOptionalString(createCharSequence( element.getKey().getValue())));
        // Set optional values in the first 50 elements to 0, ..., 49
        testData.stream().limit(50).forEach( element -> element.setOptionalValue( element.getKey() ));
        JexxaContext.init();
    }

    @AfterEach
    void deInit()
    {
        JexxaContext.cleanup();
    }
    @ParameterizedTest
    @MethodSource(REPOSITORY_CONFIG)
    void testStringComparisonOperator(Properties properties)
    {
        //Arrange
        initObjectStore(properties);

        var objectUnderTest = objectStore.getStringQuery( TestObjectSchema.STRING_OBJECT, String.class);

        //Act
        var beginsWithA = objectUnderTest.beginsWith("A");
        var endsWithA = objectUnderTest.endsWith("A");
        var equalToA = objectUnderTest.isEqualTo("A");
        var includesA = objectUnderTest.includes("A");
        var notIncludesA = objectUnderTest.notIncludes("A");

        //Assert
        assertEquals(27, beginsWithA.size()); //A + AA..AZ = 27
        assertEquals(4, endsWithA.size());    // A + AA + BA + CA = 4
        assertEquals(1, equalToA.size());     // Only 1x A
        assertEquals(29, includesA.size());  // A + AA..AZ + BA + CA= 29
        assertEquals(71, notIncludesA.size());  // 100 - 29 (includesA.size()) = 71
    }

    @ParameterizedTest
    @MethodSource(REPOSITORY_CONFIG)
    void testComparisonOperatorOptionalString(Properties properties)
    {
        //Arrange
        initObjectStore(properties);

        var objectUnderTest = objectStore.getStringQuery( TestObjectSchema.OPTIONAL_STRING_OBJECT, String.class);

        //Act
        var beginsWithA = objectUnderTest.beginsWith("A");
        var endsWithA = objectUnderTest.endsWith("A");
        var equalToA = objectUnderTest.isEqualTo("A");
        var includesA = objectUnderTest.includes("A");
        var notIncludesA = objectUnderTest.notIncludes("A");
        var equalToNull = objectUnderTest.isNull();
        var notEqualToNull = objectUnderTest.isNotNull();

        //Assert
        assertEquals(24, beginsWithA.size()); //A + AA..AW = 24
        assertEquals(2, endsWithA.size());    // A + AA = 2
        assertEquals(1, equalToA.size());     // Only 1x A
        assertEquals(24, includesA.size());   // A + AA..AW = 24
        assertEquals(26, notIncludesA.size());  // 50 - 24 (includesA.size()) = 26
        assertEquals(50, equalToNull.size());
        assertEquals(50, notEqualToNull.size());
    }

    @ParameterizedTest
    @MethodSource(REPOSITORY_CONFIG)
    void testAscendingString(Properties properties)
    {
        //Arrange
        initObjectStore(properties);
        var limit = 10;

        var objectUnderTest = objectStore.getStringQuery( TestObjectSchema.STRING_OBJECT, String.class);

        var expectedAscendingOrder = objectStore.get()
                .stream()
                .sorted(comparing(TestObject::getString))
                .toList();

        var expectedAscendingOrderLimit = expectedAscendingOrder
                .stream()
                .limit(10)
                .toList();

        //Act
        var ascendingResult = objectUnderTest.getAscending();
        var ascendingLimitResult = objectUnderTest.getAscending(limit);

        //Assert
        assertEquals(expectedAscendingOrder, ascendingResult);
        assertEquals(expectedAscendingOrderLimit, ascendingLimitResult);
    }

    @ParameterizedTest
    @MethodSource(REPOSITORY_CONFIG)
    void testDescendingString(Properties properties)
    {
        //Arrange
        initObjectStore(properties);
        var limit = 10;

        var objectUnderTest = objectStore.getStringQuery( TestObjectSchema.STRING_OBJECT, String.class);

        var expectedDescendingOrder = objectStore.get()
                .stream()
                .sorted(comparing(TestObject::getString).reversed())
                .toList();

        var expectedDescendingOrderLimit = expectedDescendingOrder
                .stream()
                .limit(10)
                .toList();

        //Act
        var descendingResult = objectUnderTest.getDescending();
        var descendingResultLimit = objectUnderTest.getDescending(limit);

        //Assert
        assertEquals(expectedDescendingOrder, descendingResult);
        assertEquals(expectedDescendingOrderLimit, descendingResultLimit);
    }

    @ParameterizedTest
    @MethodSource(REPOSITORY_CONFIG)
    void testQueryUsesLatestData(Properties properties) {
        //Arrange
        initObjectStore(properties);
        objectStore.removeAll();

        //Act
        var objectUnderTest = objectStore.getStringQuery(TestObjectSchema.STRING_OBJECT, String.class);
        testData.forEach(objectStore::add);
        var result = objectUnderTest.getAscending();

        //Assert
        assertEquals(testData.size(), result.size());
    }

    void initObjectStore(Properties properties)
    {
        if (properties.containsKey(jdbcUrl()))
        {
            try(JDBCConnection jdbcConnection = new JDBCConnection(properties))
            {
                jdbcConnection.tableCommand(TestObjectSchema.class)
                        .dropTableIfExists(TestObject.class)
                        .asIgnore();
            }
        }

        objectStore = createObjectStore(
                TestObject.class,
                TestObject::getKey,
                TestObjectSchema.class,
                properties);

        objectStore.removeAll();

        testData.forEach(objectStore::add);
    }
}

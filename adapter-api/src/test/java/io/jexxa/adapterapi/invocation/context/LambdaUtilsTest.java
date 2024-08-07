package io.jexxa.adapterapi.invocation.context;

import io.jexxa.adapterapi.invocation.function.SerializableBiConsumer;
import io.jexxa.adapterapi.invocation.function.SerializableBiFunction;
import io.jexxa.adapterapi.invocation.function.SerializableFunction;
import org.junit.jupiter.api.Test;

import static io.jexxa.adapterapi.invocation.context.LambdaUtils.methodNameFromLambda;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LambdaUtilsTest {

    @Test
    void testMethodNameFromLambda()
    {
        //Arrange
        SerializableFunction<String, String> function = LambdaUtilsTest::testFunction;
        SerializableBiFunction<String, String, String> biFunction = LambdaUtilsTest::testBiFunction;
        SerializableBiConsumer<String, String> biConsumer = LambdaUtilsTest::testBiConsumer;
        String expectedPrefix = LambdaUtilsTest.class.getName().replace('.', '/') ;

        //Act / Assert
        assertEquals(expectedPrefix + "/testFunction", methodNameFromLambda(function));
        assertEquals(expectedPrefix + "/testBiFunction", methodNameFromLambda(biFunction));
        assertEquals(expectedPrefix + "/testBiConsumer", methodNameFromLambda(biConsumer));
    }

    private static String testFunction(String string)
    {
        return string;
    }

    private static String testBiFunction(String string1, String string2)
    {
        return string1 + string2;
    }

    private static void testBiConsumer(String string1, String string2)
    {
        //Empty because we test only the signature. Since there is no logic this method is empty
    }
}

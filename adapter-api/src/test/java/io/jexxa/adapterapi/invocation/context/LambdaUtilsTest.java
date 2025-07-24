package io.jexxa.adapterapi.invocation.context;

import io.jexxa.adapterapi.invocation.function.SerializableBiConsumer;
import io.jexxa.adapterapi.invocation.function.SerializableBiFunction;
import io.jexxa.adapterapi.invocation.function.SerializableFunction;
import org.junit.jupiter.api.Test;

import static io.jexxa.adapterapi.invocation.context.LambdaUtils.ANONYMOUS_METHOD_NAME;
import static io.jexxa.adapterapi.invocation.context.LambdaUtils.classNameFromLambda;
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
        SerializableFunction<String, String> anonymousConsumer = data -> data;

        //Act / Assert
        assertEquals("testFunction", methodNameFromLambda(function));
        assertEquals("testBiFunction", methodNameFromLambda(biFunction));
        assertEquals("testBiConsumer", methodNameFromLambda(biConsumer));
        assertEquals(ANONYMOUS_METHOD_NAME, methodNameFromLambda(anonymousConsumer));
    }

    @Test
    void testClassNameFromLambda()
    {
        //Arrange
        SerializableFunction<String, String> function = LambdaUtilsTest::testFunction;
        SerializableBiFunction<String, String, String> biFunction = LambdaUtilsTest::testBiFunction;
        SerializableBiConsumer<String, String> biConsumer = LambdaUtilsTest::testBiConsumer;
        SerializableFunction<String, String> anonymousConsumer = data -> data;

        //Act / Assert
        assertEquals(LambdaUtilsTest.class.getName(), classNameFromLambda(function).getName());
        assertEquals(LambdaUtilsTest.class.getName(), classNameFromLambda(biFunction).getName());
        assertEquals(LambdaUtilsTest.class.getName(), classNameFromLambda(biConsumer).getName());
        assertEquals(LambdaUtilsTest.class.getName(), classNameFromLambda( anonymousConsumer ).getName());

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

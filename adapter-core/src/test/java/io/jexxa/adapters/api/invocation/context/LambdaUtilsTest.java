package io.jexxa.adapters.api.invocation.context;

import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class LambdaUtilsTest {

    @FunctionalInterface
    interface SerializableSupplier<T> extends Supplier<T>, Serializable {}

    @FunctionalInterface
    interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}

    @FunctionalInterface
    interface SerializableBiFunction<T, U, R> extends BiFunction<T, U, R>, Serializable {}

    static class DummyTarget {
        public String simpleMethod() {
            return "result";
        }

        public int primitiveMethod(int number) {
            return number * 2;
        }

        public String superTypeMethod(CharSequence text) {
            return text.toString();
        }
    }

    @Test
    void methodNameFromLambda_withMethodReference_returnsMethodName() {
        // Arrange
        DummyTarget target = new DummyTarget();
        SerializableSupplier<String> lambda = target::simpleMethod;

        // Act
        String result = LambdaUtils.methodNameFromLambda(lambda);

        // Assert
        assertEquals("simpleMethod", result);
    }

    @Test
    void methodNameFromLambda_withAnonymousLambda_returnsAnonymousMethodName() {
        // Arrange
        SerializableSupplier<String> lambda = () -> "anonymous";

        // Act
        String result = LambdaUtils.methodNameFromLambda(lambda);

        // Assert
        assertEquals(LambdaUtils.ANONYMOUS_METHOD_NAME, result);
    }

    @Test
    void methodNameFromLambda_withNonLambdaSerializable_returnsUnknownMethodName() {
        // Arrange
        Serializable nonLambda = "Just a normal String";

        // Act
        String result = LambdaUtils.methodNameFromLambda(nonLambda);

        // Assert
        assertEquals("unknownMethodName", result);
    }

    @Test
    void classNameFromLambda_withMethodReference_returnsDeclaringClass() {
        // Arrange
        DummyTarget target = new DummyTarget();
        SerializableSupplier<String> lambda = target::simpleMethod;

        // Act
        Class<?> result = LambdaUtils.classNameFromLambda(lambda);

        // Assert
        assertEquals(DummyTarget.class, result);
    }

    @Test
    void classNameFromLambda_withNonLambdaSerializable_returnsObjectClass() {
        // Arrange
        Serializable nonLambda = 42;

        // Act
        Class<?> result = LambdaUtils.classNameFromLambda(nonLambda);

        // Assert
        assertEquals(Object.class, result);
    }

    @Test
    void getSerializedLambda_withValidLambda_returnsSerializedLambda() {
        // Arrange
        DummyTarget target = new DummyTarget();
        SerializableSupplier<String> lambda = target::simpleMethod;

        // Act
        SerializedLambda result = LambdaUtils.getSerializedLambda(lambda);

        // Assert
        assertNotNull(result);
        assertEquals("simpleMethod", result.getImplMethodName());
    }

    @Test
    void getSerializedLambda_withNonLambdaSerializable_returnsNull() {
        // Arrange
        Serializable nonLambda = "Not a lambda";

        // Act
        SerializedLambda result = LambdaUtils.getSerializedLambda(nonLambda);

        // Assert
        assertNull(result);
    }

    @Test
    void getImplMethod_withExactMatch_returnsMethod() throws NoSuchMethodException {
        // Arrange
        DummyTarget target = new DummyTarget();
        SerializableBiFunction<DummyTarget, CharSequence, String> lambda = DummyTarget::superTypeMethod;
        Class<?>[] argTypes = new Class[]{CharSequence.class};

        // Act
        Method result = LambdaUtils.getImplMethod(target, lambda, argTypes);

        // Assert
        assertEquals(DummyTarget.class.getMethod("superTypeMethod", CharSequence.class), result);
    }

    @Test
    void getImplMethod_withSubtypeArgument_returnsMethodViaIsAssignable() throws NoSuchMethodException {
        // Arrange
        DummyTarget target = new DummyTarget();
        SerializableBiFunction<DummyTarget, CharSequence, String> lambda = DummyTarget::superTypeMethod;
        Class<?>[] argTypes = new Class[]{String.class}; // String implementiert CharSequence

        // Act
        Method result = LambdaUtils.getImplMethod(target, lambda, argTypes);

        // Assert
        assertEquals(DummyTarget.class.getMethod("superTypeMethod", CharSequence.class), result);
    }

    @Test
    void getImplMethod_withWrapperTypeForPrimitiveParameter_resolvesPrimitiveMethod() throws NoSuchMethodException {
        // Arrange
        DummyTarget target = new DummyTarget();
        SerializableBiFunction<DummyTarget, Integer, Integer> lambda = DummyTarget::primitiveMethod;
        Class<?>[] argTypes = new Class[]{Integer.class}; // Wrapper statt int.class

        // Act
        Method result = LambdaUtils.getImplMethod(target, lambda, argTypes);

        // Assert
        assertEquals(DummyTarget.class.getMethod("primitiveMethod", int.class), result);
    }

    @Test
    void getImplMethod_withNonExistentMethodOrWrongArgs_throwsIllegalArgumentException() {
        // Arrange
        DummyTarget target = new DummyTarget();
        SerializableFunction<DummyTarget, String> lambda = DummyTarget::simpleMethod;
        Class<?>[] wrongArgTypes = new Class[]{String.class};

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                LambdaUtils.getImplMethod(target, lambda, wrongArgTypes)
        );
    }
}

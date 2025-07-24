package io.jexxa.adapterapi.invocation.context;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class LambdaUtils {

    public static final String ANONYMOUS_METHOD_NAME = "anonymousMethod";
    private static final String WRITE_REPLACE = "writeReplace";

    private static final Map<Class<?>, Class<?>> WRAPPER_TYPE_MAP;
    static {
        WRAPPER_TYPE_MAP = new HashMap<>(16);
        WRAPPER_TYPE_MAP.put(Integer.class, int.class);
        WRAPPER_TYPE_MAP.put(Byte.class, byte.class);
        WRAPPER_TYPE_MAP.put(Character.class, char.class);
        WRAPPER_TYPE_MAP.put(Boolean.class, boolean.class);
        WRAPPER_TYPE_MAP.put(Double.class, double.class);
        WRAPPER_TYPE_MAP.put(Float.class, float.class);
        WRAPPER_TYPE_MAP.put(Long.class, long.class);
        WRAPPER_TYPE_MAP.put(Short.class, short.class);
        WRAPPER_TYPE_MAP.put(Void.class, void.class);
    }

    @SuppressWarnings("java:S3011") //required for setAccessible(true)
    public static String methodNameFromLambda(Serializable lambda) {
        try {
            Method lambdaMethod = lambda.getClass().getDeclaredMethod(WRITE_REPLACE);
            lambdaMethod.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) lambdaMethod.invoke(lambda);
            if (serializedLambda.getImplMethodName().startsWith("lambda$"))
            {
                return ANONYMOUS_METHOD_NAME;
            }
            return serializedLambda.getImplMethodName();
        } catch (ReflectiveOperationException ex) {
            return "unknownMethodName";
        }
    }

    @SuppressWarnings("java:S3011") //required for setAccessible(true)
    public static  Class<?> classNameFromLambda(Serializable lambda) {
        try {
            Method writeReplace = lambda.getClass().getDeclaredMethod(WRITE_REPLACE);
            writeReplace.setAccessible(true);
            SerializedLambda serialized = (SerializedLambda) writeReplace.invoke(lambda);
            String className = serialized.getImplClass().replace('/', '.');
            return Class.forName(className);
        } catch (ReflectiveOperationException e)
        {
            return Object.class;
        }
    }

    static  <T extends Serializable> Method getImplMethod(Object targetObject, T functionalInterface, Class<?>[] argTypes)
    {
        try {
            var serializedLambda = Objects.requireNonNull(getSerializedLambda(functionalInterface));

            return Arrays.stream(targetObject
                    .getClass()
                    .getMethods())
                    .filter(element -> element.getName().equals(serializedLambda.getImplMethodName()))
                    .filter(element -> isAssignable(element.getParameterTypes(), argTypes))
                    .findAny().orElseThrow(() -> new NoSuchMethodException( "Method not found " + targetObject.getClass() + "::" + serializedLambda.getImplMethodName() ));
        } catch (NoSuchMethodException e) { // Check if an alternative method with primitive types is available
            if (includePrimitives(argTypes))
            {
                return getImplMethod(targetObject, functionalInterface, convertToPrimitives(argTypes));
            }
            throw new IllegalArgumentException(e);
        } catch ( SecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * This method extracts the SerializedLambda from a functional interface. To ensure that this is available,
     * the functional interface must implement Serializable which ensures that method `writeReplace` is automatically
     * generated
     *
     * @param functionalInterface from which SerializedLambda should be extracted
     * @param <T> Type of the functionalInterface
     * @return SerializedLambda of the functional interface
     */
    @SuppressWarnings("java:S3011")
    static <T extends Serializable> SerializedLambda getSerializedLambda(T functionalInterface) {
        SerializedLambda serializedLambda = null;
        for (Class<?> clazz = functionalInterface.getClass(); clazz != null; clazz = clazz.getSuperclass())
        {
            try {
                Method replaceMethod = clazz.getDeclaredMethod(WRITE_REPLACE);
                replaceMethod.setAccessible(true);
                Object serialVersion = replaceMethod.invoke(functionalInterface);

                // check if class is a lambda function
                if (serialVersion != null && serialVersion.getClass() == SerializedLambda.class) {
                    serializedLambda = (SerializedLambda) serialVersion;
                    break;
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // thrown if the method is not there. fall through the loop
            }
        }
        return serializedLambda;
    }

    private static Class<?>[] convertToPrimitives(Class<?>[] types)
    {
        var result = new Class<?>[types.length];
        for (int i = 0; i< types.length; ++i)
        {
           result[i] = convertToPrimitive(types[i]);
        }

        return result;
    }

    private static Class<?> convertToPrimitive(Class<?> clazz)
    {
        if (WRAPPER_TYPE_MAP.containsKey(clazz))
        {
            return WRAPPER_TYPE_MAP.get(clazz);
        }

        return clazz;
    }

    private static boolean includePrimitives(Class<?>[] types)
    {
        for (Class<?> type : types) {
            if (WRAPPER_TYPE_MAP.containsKey(type)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAssignable(Class<?>[] original, Class<?>[] assignee)
    {
        if (original.length != assignee.length)
        {
            return false;
        }

        for (int i = 0; i< original.length; i++)
        {
            if ( !original[i].isAssignableFrom(assignee[i]))
            {
                return false;
            }
        }

        return true;
    }



    private LambdaUtils()
    {
        /* Private constructor */
    }



}

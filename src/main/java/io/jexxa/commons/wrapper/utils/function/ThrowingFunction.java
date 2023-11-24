package io.jexxa.commons.wrapper.utils.function;


@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T u) throws E;
}


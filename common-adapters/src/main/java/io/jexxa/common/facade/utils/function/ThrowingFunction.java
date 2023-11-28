package io.jexxa.common.facade.utils.function;


@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T u) throws E;
}


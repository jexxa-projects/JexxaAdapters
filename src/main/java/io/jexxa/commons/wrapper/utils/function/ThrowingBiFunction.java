package io.jexxa.commons.wrapper.utils.function;


@FunctionalInterface
public interface ThrowingBiFunction<U, V, R, E extends Exception> {
    R apply(U u, V v) throws E;
}


package io.jexxa.common.facade.utils.function;


import java.io.Serializable;

@FunctionalInterface
public interface ThrowingBiFunction<U, V, R, E extends Exception> extends Serializable {
    R apply(U u, V v) throws E;
}


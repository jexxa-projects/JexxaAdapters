package io.jexxa.common.facade.utils.function;


import java.io.Serializable;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> extends Serializable {
    R apply(T u) throws E;
}


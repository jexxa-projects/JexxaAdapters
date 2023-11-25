package io.jexxa.common.facade.utils.function;

@FunctionalInterface
public interface ThrowingBiConsumer<U, V, E extends Exception> {
    void accept(U u, V v) throws E;
}


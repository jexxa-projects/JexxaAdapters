package io.jexxa.adapters.core.facade.utils.function;

import java.io.Serializable;

@FunctionalInterface
public interface ThrowingBiConsumer<U, V, E extends Exception> extends Serializable {
    void accept(U u, V v) throws E;
}


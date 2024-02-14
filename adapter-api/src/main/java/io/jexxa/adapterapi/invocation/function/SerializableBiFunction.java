package io.jexxa.adapterapi.invocation.function;

import java.io.Serializable;
import java.util.function.BiFunction;

@FunctionalInterface
public interface SerializableBiFunction<U, V, R> extends BiFunction<U, V, R>, Serializable {
    // Functional interface for a BiFunction that can be serialized
}

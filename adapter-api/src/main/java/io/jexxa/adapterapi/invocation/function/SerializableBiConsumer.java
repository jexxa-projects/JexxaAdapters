package io.jexxa.adapterapi.invocation.function;

import java.io.Serializable;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface SerializableBiConsumer<T, U> extends BiConsumer<T, U>, Serializable {
    // Functional interface for a BiConsumer that can be serialized
}

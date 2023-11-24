package wrapper.utils.function;


@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T u) throws E;
}


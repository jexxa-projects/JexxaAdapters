package io.jexxa.common.messaging;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public final class InProcessPubSub
{
    private final Map<Class<?>, Set<Consumer<?>>> subscribers = new HashMap<>();

    public InProcessPubSub()
    {
        // Public constructor
    }

    @SuppressWarnings("unchecked")
    public <T> void publish(final T message)
    {
        Objects.requireNonNull(message, "Message must not be null");

        final List<Consumer<T>> matchingSubscribers;

        // Create a snapshot of matching subscribers
        synchronized (this)
        {
            matchingSubscribers = subscribers.entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().isAssignableFrom(message.getClass()))
                    .flatMap(entry -> entry.getValue().stream())
                    .map(subscriber -> (Consumer<T>) subscriber)
                    .toList();
        }

        // Notify subscribers outside the synchronized block
        matchingSubscribers.forEach(subscriber -> subscriber.accept(message));
    }

    public synchronized <T> void subscribe(
            Class<T> messageType,
            Consumer<T> subscriber)
    {
        Objects.requireNonNull(messageType, "Message type must not be null");
        Objects.requireNonNull(subscriber, "Subscriber must not be null");

        subscribers
                .computeIfAbsent(messageType, _ -> new HashSet<>())
                .add(subscriber);
    }

    public void subscribe(Consumer<Object> subscriber)
    {
        subscribe(Object.class, subscriber);
    }
}
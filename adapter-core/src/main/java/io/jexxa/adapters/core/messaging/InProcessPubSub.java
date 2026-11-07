package io.jexxa.adapters.core.messaging;

import java.util.ArrayList;
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
    private final Map<String, Set<Consumer<?>>> topicSubscribers = new HashMap<>();

    private static final InProcessPubSub INSTANCE = new InProcessPubSub();

    public static InProcessPubSub instance() { return INSTANCE; }

    public InProcessPubSub()
    {
        // Public constructor
    }

    @SuppressWarnings("unchecked")
    public <T> void publish(final T message)
    {
        Objects.requireNonNull(message, "Message must not be null");

        final List<Consumer<T>> matchingSubscribers = new ArrayList<>();

        // Create a snapshot of matching subscribers (Class-based + ALL Topic-based as Broadcast)
        synchronized (this)
        {
            // 1. Existing class-based subscribers
            subscribers.entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().isAssignableFrom(message.getClass()))
                    .flatMap(entry -> entry.getValue().stream())
                    .map(subscriber -> (Consumer<T>) subscriber)
                    .forEach(matchingSubscribers::add);

            // 2. Broadcast to ALL string topics
            topicSubscribers.values()
                    .stream()
                    .flatMap(Set::stream)
                    .map(subscriber -> (Consumer<T>) subscriber)
                    .forEach(matchingSubscribers::add);
        }

        // Notify subscribers outside the synchronized block
        matchingSubscribers.forEach(subscriber -> subscriber.accept(message));
    }

    @SuppressWarnings("unchecked")
    public <T> void publish(final String topic, final T message)
    {
        Objects.requireNonNull(topic, "Topic must not be null");
        Objects.requireNonNull(message, "Message must not be null");

        final List<Consumer<T>> matchingSubscribers;

        // Create a snapshot of subscribers for this specific topic
        synchronized (this)
        {
            Set<Consumer<?>> listeners = topicSubscribers.get(topic);
            if (listeners != null)
            {
                matchingSubscribers = listeners.stream()
                        .map(subscriber -> (Consumer<T>) subscriber)
                        .toList();
            }
            else
            {
                matchingSubscribers = List.of();
            }
        }

        // Notify subscribers outside the synchronized block
        matchingSubscribers.forEach(subscriber -> subscriber.accept(message));
    }

    public synchronized <T> void subscribe(Class<T> messageType, Consumer<T> subscriber)
    {
        Objects.requireNonNull(messageType, "Message type must not be null");
        Objects.requireNonNull(subscriber, "Subscriber must not be null");

        subscribers
                .computeIfAbsent(messageType, _ -> new HashSet<>())
                .add(subscriber);
    }

    public synchronized <T> void subscribe(String topic, Consumer<T> subscriber)
    {
        Objects.requireNonNull(topic, "Topic must not be null");
        Objects.requireNonNull(subscriber, "Subscriber must not be null");

        topicSubscribers
                .computeIfAbsent(topic, _ -> new HashSet<>())
                .add(subscriber);
    }

    public void subscribe(Consumer<Object> subscriber)
    {
        subscribe(Object.class, subscriber);
    }
}

package io.jexxa.adapters.core.drivenadapter.messaging;

import io.jexxa.adapters.core.facade.factory.ClassFactory;
import io.jexxa.adapters.core.facade.logger.ApplicationBanner;
import io.jexxa.adapters.core.facade.utils.annotation.CheckReturnValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;

import static io.jexxa.adapters.core.drivenadapter.messaging.MessageSenderProperties.messageSenderStrategy;
import static io.jexxa.adapters.core.facade.logger.SLF4jLogger.getLogger;


@SuppressWarnings("java:S6548")
public final class MessageSenderFactory
{
    private static final MessageSenderFactory MESSAGE_SENDER_FACTORY = new MessageSenderFactory();

    private static final Map<Class<?> , Class<? extends MessageSender>> STRATEGY_MAP = new HashMap<>();
    private static Class<? extends MessageSender> defaultMessageSender = null;

    @SuppressWarnings("unused")
    public static Class<?> getDefaultMessageSender(Properties properties)
    {
        return createMessageSender(null, properties).getClass();
    }

    @SuppressWarnings("unused")
    public static <U extends MessageSender, T > void setMessageSender(Class<U> messageSender, Class<T> aggregateType)
    {
        STRATEGY_MAP.put(aggregateType, messageSender);
    }

    public static void setDefaultMessageSender(Class<? extends MessageSender>  defaultMessageSender)
    {
        Objects.requireNonNull(defaultMessageSender);

        MessageSenderFactory.defaultMessageSender = defaultMessageSender;
    }


    public static <T>  MessageSender createMessageSender(Class<T> sendingClass, Properties properties)
    {
        try
        {
            var strategy = MESSAGE_SENDER_FACTORY.messageSenderType(sendingClass, properties);

            var result = ClassFactory.newInstanceOf(strategy, new Object[]{properties});
            if (result.isEmpty()) //Try a factory method with properties
            {
                result = ClassFactory.newInstanceOf(MessageSender.class, strategy,new Object[]{properties});
            }
            if (result.isEmpty()) //Try default constructor
            {
                result = ClassFactory.newInstanceOf(strategy);
            }
            if (result.isEmpty()) //Try a factory method without properties
            {
                result = ClassFactory.newInstanceOf(MessageSender.class, strategy);
            }

            return result.orElseThrow();
        }
        catch (ReflectiveOperationException e)
        {
            if ( e.getCause() != null)
            {
                throw new IllegalArgumentException(e.getCause().getMessage(), e);
            }

            throw new IllegalArgumentException("No suitable default MessageSender available", e);
        }
    }

    public void bannerInformation(Properties properties)
    {
        getLogger(ApplicationBanner.class).info("Used Message Sender Strategie  : [{}]", MESSAGE_SENDER_FACTORY.messageSenderType(null, properties).getSimpleName());
    }

    private MessageSenderFactory()
    {
        ApplicationBanner.addConfigBanner(this::bannerInformation);
    }

    @CheckReturnValue
    @SuppressWarnings("unchecked")
    private <T> Class<? extends MessageSender> messageSenderType(Class<T> aggregateClazz, Properties properties)
    {
        // 1. Check if a dedicated strategy is registered for aggregateClazz
        var result = STRATEGY_MAP
                .entrySet()
                .stream()
                .filter( element -> element.getKey().equals(aggregateClazz))
                .filter( element -> element.getValue() != null )
                .findFirst();

        if (result.isPresent())
        {
            return result.get().getValue();
        }

        // 2. If a default strategy is available, return this one
        if (defaultMessageSender != null)
        {
            return defaultMessageSender;
        }

        // 3. Check explicit configuration
        if (properties.containsKey(messageSenderStrategy()))
        {
            try {
                return (Class<? extends MessageSender>) Class.forName(properties.getProperty(messageSenderStrategy()));
            } catch (ClassNotFoundException e)
            {
                getLogger(MessageSenderFactory.class).warn("Unknown or invalid message sender {} -> Ignore setting", properties.getProperty(messageSenderStrategy()));
                getLogger(MessageSenderFactory.class).warn(String.valueOf(e));
            }
        }

        // 4. Discover matching MessageSender providers
        var matches = ServiceLoader.load(MessageSenderProvider.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .map(provider -> provider.getMessageSenderType(properties))
                .flatMap(Optional::stream)
                .distinct()
                .toList();

        if (matches.size() > 1)
        {
            throw new IllegalStateException(
                    "Multiple message sender strategies match: " + matches
            );
        }

        if (!matches.isEmpty())
        {
            return matches.getFirst();
        }

        // 4. Fallback
        return MessageLogger.class;
    }



}

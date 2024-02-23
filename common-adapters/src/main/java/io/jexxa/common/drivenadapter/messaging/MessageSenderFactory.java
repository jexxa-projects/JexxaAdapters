package io.jexxa.common.drivenadapter.messaging;

import io.jexxa.common.drivenadapter.messaging.logging.MessageLogger;
import io.jexxa.common.drivenadapter.outbox.TransactionalOutboxSender;
import io.jexxa.common.facade.factory.ClassFactory;
import io.jexxa.common.facade.logger.ApplicationBanner;
import io.jexxa.common.facade.utils.annotation.CheckReturnValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static io.jexxa.common.facade.jms.JMSProperties.JNDI_FACTORY_KEY;
import static io.jexxa.common.facade.jms.JMSProperties.jmsSimulate;
import static io.jexxa.common.facade.jms.JMSProperties.jmsStrategy;
import static io.jexxa.common.facade.logger.SLF4jLogger.getLogger;


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
            if (result.isEmpty()) //Try factory method with properties
            {
                result = ClassFactory.newInstanceOf(MessageSender.class, strategy,new Object[]{properties});
            }
            if (result.isEmpty()) //Try default constructor
            {
                result = ClassFactory.newInstanceOf(strategy);
            }
            if (result.isEmpty()) //Try factory method without properties
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
        if (properties.containsKey(jmsStrategy()))
        {
            try {
                return (Class<? extends MessageSender>) Class.forName(properties.getProperty(jmsStrategy()));
            } catch (ClassNotFoundException e)
            {
                getLogger(MessageSenderFactory.class).warn("Unknown or invalid message sender {} -> Ignore setting", properties.getProperty(jmsStrategy()));
                getLogger(MessageSenderFactory.class).warn(String.valueOf(e));
            }
        }

        // 4. If a JNDI Factory is defined and simulation mode is deactivated => Use TransactionalOutboxSender
        if (properties.containsKey(JNDI_FACTORY_KEY) && !properties.containsKey(jmsSimulate()))
        {
            return TransactionalOutboxSender.class;
        }

        // 5. In all other cases (including simulation mode) return a MessageLogger
        return MessageLogger.class;
    }



}

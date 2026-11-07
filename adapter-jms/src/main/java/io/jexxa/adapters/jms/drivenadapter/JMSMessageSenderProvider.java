package io.jexxa.adapters.jms.drivenadapter;

import io.jexxa.adapters.core.drivenadapter.messaging.MessageSender;
import io.jexxa.adapters.core.drivenadapter.messaging.MessageSenderProvider;
import io.jexxa.adapters.core.facade.logger.SLF4jLogger;

import java.util.Optional;
import java.util.Properties;

import static io.jexxa.adapters.jms.facade.JMSProperties.jmsSimulate;
import static io.jexxa.adapters.jms.facade.JMSProperties.jndiFactoryKey;

public class JMSMessageSenderProvider implements MessageSenderProvider
{
    @Override
    public Optional<Class<? extends MessageSender>> getMessageSenderType(
            Properties properties)
    {
        if (properties.containsKey(jndiFactoryKey())
                && !properties.containsKey(jmsSimulate()))
        {
            return Optional.of(TransactionalOutboxSender.class);
        }
        SLF4jLogger.getLogger(JMSMessageSenderProvider.class).warn("JMSMessageSenderProvider is not configured");

        return Optional.empty();
    }
}
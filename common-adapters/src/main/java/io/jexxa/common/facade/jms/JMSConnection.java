package io.jexxa.common.facade.jms;


import io.jexxa.common.facade.utils.properties.Secret;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

import static io.jexxa.common.facade.jms.JMSProperties.jndiFactoryKey;
import static io.jexxa.common.facade.jms.JMSProperties.jndiPasswordFile;
import static io.jexxa.common.facade.jms.JMSProperties.jndiPasswordKey;
import static io.jexxa.common.facade.jms.JMSProperties.jndiProviderUrlKey;
import static io.jexxa.common.facade.jms.JMSProperties.jndiUserFile;
import static io.jexxa.common.facade.jms.JMSProperties.jndiUserKey;
import static io.jexxa.common.facade.utils.properties.PropertiesPrefix.prefix;
import static io.jexxa.common.facade.utils.properties.PropertiesUtils.removePrefixFromKeys;

public class JMSConnection {
    public static Connection createConnection(Properties properties)
    {
        validateProperties(properties);
        var username = new Secret(properties, jndiUserKey(), jndiUserFile());
        var password = new Secret(properties, jndiPasswordKey(), jndiPasswordFile());

        var jmsProperties = removePrefixFromKeys(properties, prefix());
        try
        {
            var initialContext = new InitialContext(jmsProperties);
            var connectionFactory = (ConnectionFactory) initialContext.lookup("ConnectionFactory");
            return connectionFactory.createConnection(username.getSecret(), password.getSecret());
        }
        catch (NamingException e)
        {
            throw new IllegalStateException("No ConnectionFactory available via : " + properties.get(jndiProviderUrlKey()), e);
        }
        catch (JMSException e)
        {
            throw new IllegalStateException("Can not connect to " + properties.get(jndiProviderUrlKey()), e);
        }
    }

    private static void validateProperties(Properties properties)
    {
        if (!properties.containsKey(jndiProviderUrlKey()))
        {
            throw new IllegalArgumentException("Missing JMS properties: " + jndiProviderUrlKey());
        }

        if (!properties.containsKey(jndiFactoryKey()))
        {
            throw new IllegalArgumentException("Missing JMS properties: " + jndiFactoryKey());
        }
    }
    private JMSConnection()
    {
        //Private constructor
    }
}

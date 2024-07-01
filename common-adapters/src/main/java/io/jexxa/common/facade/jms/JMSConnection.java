package io.jexxa.common.facade.jms;


import io.jexxa.common.facade.utils.properties.Secret;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

import static io.jexxa.common.facade.jms.JMSProperties.JNDI_FACTORY_KEY;
import static io.jexxa.common.facade.jms.JMSProperties.JNDI_PROVIDER_URL_KEY;

public class JMSConnection {
    public static Connection createConnection(Properties properties)
    {
        validateProperties(properties);
        var username = new Secret(properties, JMSProperties.JNDI_USER_KEY, JMSProperties.JNDI_USER_FILE);
        var password = new Secret(properties, JMSProperties.JNDI_PASSWORD_KEY, JMSProperties.JNDI_PASSWORD_FILE);

        try
        {
            var initialContext = new InitialContext(properties);
            var connectionFactory = (ConnectionFactory) initialContext.lookup("ConnectionFactory");
            return connectionFactory.createConnection(username.getSecret(), password.getSecret());
        }
        catch (NamingException e)
        {
            throw new IllegalStateException("No ConnectionFactory available via : " + properties.get(JNDI_PROVIDER_URL_KEY), e);
        }
        catch (JMSException e)
        {
            throw new IllegalStateException("Can not connect to " + properties.get(JNDI_PROVIDER_URL_KEY), e);
        }
    }

    private static void validateProperties(Properties properties)
    {
        if (!properties.containsKey(JNDI_PROVIDER_URL_KEY))
        {
            throw new IllegalArgumentException("Missing JMS properties: " + JNDI_PROVIDER_URL_KEY);
        }

        if (!properties.containsKey(JNDI_FACTORY_KEY))
        {
            throw new IllegalArgumentException("Missing JMS properties: " + JNDI_FACTORY_KEY);
        }
    }
    private JMSConnection()
    {
        //Private constructor
    }
}

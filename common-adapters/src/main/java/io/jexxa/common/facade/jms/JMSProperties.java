package io.jexxa.common.facade.jms;

import static io.jexxa.adapterapi.PropertiesPrefix.prefix;

public final class JMSProperties {
    public static final String JNDI_PROVIDER_URL_KEY = "java.naming.provider.url";
    public static final String JNDI_USER_KEY = "java.naming.user";
    public static final String JNDI_PASSWORD_KEY = "java.naming.password";
    public static final String JNDI_FACTORY_KEY = "java.naming.factory.initial";
    public static final String JNDI_PASSWORD_FILE = "java.naming.file.password";
    public static final String JNDI_USER_FILE = "java.naming.file.user";
    public static final String JNDI_CLIENT_ID = "java.naming.client.id";

    public static String jmsStrategy() { return prefix() + "jms.strategy"; }
    public static String jmsSimulate() { return prefix() + "jms.simulate"; }

    private JMSProperties()
    {
        //private constructor
    }
}

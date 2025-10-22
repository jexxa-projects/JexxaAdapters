package io.jexxa.common.facade.jms;

import static io.jexxa.common.facade.utils.properties.PropertiesPrefix.prefix;

public final class JMSProperties {
    private static final String JNDI_PROVIDER_URL_KEY = "java.naming.provider.url";
    private static final String JNDI_USER_KEY = "java.naming.user";
    private static final String JNDI_PASSWORD_KEY = "java.naming.password";
    private static final String JNDI_FACTORY_KEY = "java.naming.factory.initial";
    private static final String JNDI_PASSWORD_FILE = "java.naming.file.password";
    private static final String JNDI_USER_FILE = "java.naming.file.user";
    private static final String JNDI_CLIENT_ID = "java.naming.client.id";

    public static String jmsStrategy() { return prefix() + "jms.strategy"; }
    public static String jmsSimulate() { return prefix() + "jms.simulate"; }

    public static String jndiProviderUrlKey() {
        return prefix() + JNDI_PROVIDER_URL_KEY;
    }

    public static String jndiUserKey() {
        return prefix() + JNDI_USER_KEY;
    }

    public static String jndiPasswordKey() {
        return prefix() + JNDI_PASSWORD_KEY;
    }

    public static String jndiPasswordFile() {
        return prefix() + JNDI_PASSWORD_FILE;
    }

    public static String jndiFactoryKey() {
        return prefix() + JNDI_FACTORY_KEY;
    }

    public static String jndiUserFile() {
        return prefix() + JNDI_USER_FILE;
    }

    public static String jndiClientId() {
        return prefix() + JNDI_CLIENT_ID;
    }


    private JMSProperties()
    {
        //private constructor
    }
}

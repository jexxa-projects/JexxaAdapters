package io.jexxa.common.facade.jms;

public final class JMSProperties {
    private static String prefix = "";
    public static final String JNDI_PROVIDER_URL_KEY = "java.naming.provider.url";
    public static final String JNDI_USER_KEY = "java.naming.user";
    public static final String JNDI_PASSWORD_KEY = "java.naming.password";
    public static final String JNDI_FACTORY_KEY = "java.naming.factory.initial";
    public static final String JNDI_PASSWORD_FILE = "java.naming.file.password";
    public static final String JNDI_USER_FILE = "java.naming.file.user";
    public static final String JNDI_CLIENT_ID = "java.naming.client.id";

    public static String jmsStrategy() {return prefix + "jms.strategy"; }
    public static String jmsSimulate() { return prefix + "jms.simulate"; }

    public static void prefix(String prefix) { JMSProperties.prefix = prefix;}
    public static String prefix() { return JMSProperties.prefix; }
    private JMSProperties()
    {
        //private constructor
    }
}

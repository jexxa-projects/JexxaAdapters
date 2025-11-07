package io.jexxa.common.facade.utils.properties;

public class PropertiesPrefix {
    private static String globalPropertiesPrefix = "";
    public static String globalPrefix() { return globalPropertiesPrefix; }
    public static void globalPrefix(String propertiesPrefix) { PropertiesPrefix.globalPropertiesPrefix = propertiesPrefix; }

    private PropertiesPrefix()
    {
        //Hide public constructor
    }
}

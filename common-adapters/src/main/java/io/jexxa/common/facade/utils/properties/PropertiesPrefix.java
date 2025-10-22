package io.jexxa.common.facade.utils.properties;

public class PropertiesPrefix {
    private static String propertiesPrefix = "";
    public static String prefix() { return propertiesPrefix; }
    public static void prefix(String propertiesPrefix) { PropertiesPrefix.propertiesPrefix = propertiesPrefix; }
}

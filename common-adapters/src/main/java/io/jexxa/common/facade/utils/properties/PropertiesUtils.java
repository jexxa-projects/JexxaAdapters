package io.jexxa.common.facade.utils.properties;

import java.util.List;
import java.util.Properties;

public class PropertiesUtils {

    public static Properties getSubset(Properties properties, String propertiesPrefix)
    {
        var subset = new Properties();
        List<String> result = properties.keySet().stream()
                .map(Object::toString)
                .filter(string -> string.contains(propertiesPrefix))
                .toList();

        result.forEach( element -> subset.put(
                element.substring(element.lastIndexOf(propertiesPrefix) + propertiesPrefix.length() + 1),
                properties.getProperty(element) ));

        return subset;
    }

    private PropertiesUtils()
    {
        //Private constructor because class provides only static methods
    }
}

package io.jexxa.common.facade.utils.properties;

import java.util.List;
import java.util.Properties;

public class PropertiesUtils {

    /**
     * Returns a new Properties object with the specified prefix removed from
     * all keys that start with it. Keys not starting with the prefix remain unchanged.
     *
     * <p><strong>Example:</strong></p>
     * <pre>
     * Properties props = new Properties();
     * props.setProperty("db.url", "localhost");
     * props.setProperty("db.user", "admin");
     * props.setProperty("app.name", "MyApp");
     *
     * Properties newProps = removePrefixFromKeys(props, "db.");
     * // newProps contains:
     * //   url=localhost
     * //   user=admin
     * //   app.name=MyApp
     * </pre>
     *
     * @param properties the original Properties object
     * @param prefix the prefix to remove from property keys
     * @return a new Properties object with the prefix removed from matching keys
     */
    public static Properties removePrefixFromKeys(Properties properties, String prefix) {
        Properties result = new Properties();

        properties.stringPropertyNames().forEach(key -> {
            String newKey = key.startsWith(prefix) ? key.substring(prefix.length()) : key;
            result.setProperty(newKey, properties.getProperty(key)); // überschreibt ggf.
        });

        return result;
    }

    /**
     * Filters the properties and returns a subset containing only the entries
     * whose keys start with the specified prefix.
     * <p>
     * In the returned {@link Properties} object, the prefix is removed from each
     * key. Properties whose keys do not start with the prefix are excluded.
     * </p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>
     * Properties props = new Properties();
     * props.setProperty("db.url", "localhost");
     * props.setProperty("db.user", "admin");
     * props.setProperty("app.name", "MyApp");
     *
     * Properties dbProps = getSubset(props, "db.");
     * // dbProps contains:
     * //   url=localhost
     * //   user=admin
     * </pre>
     *
     * @param properties the original set of properties to filter
     * @param propertiesPrefix the prefix to match against property keys
     * @return a new {@link Properties} object containing only the entries whose
     *         keys start with the specified prefix, with the prefix removed
     */
    public static Properties filterByPrefix(Properties properties, String propertiesPrefix)
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

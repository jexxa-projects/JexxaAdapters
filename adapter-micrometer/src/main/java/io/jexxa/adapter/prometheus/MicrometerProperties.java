package io.jexxa.adapter.prometheus;

import io.jexxa.adapters.core.facade.utils.properties.PropertiesPrefix;

public class MicrometerProperties {
    public static final String PROMETHEUS_PORT      =  "prometheus.port";
    public static final String PROMETHEUS_ENDPOINT  =  "prometheus.endpoint";
    public static String prometheusEndpoint() {
        return PropertiesPrefix.globalPrefix() + PROMETHEUS_ENDPOINT;
    }

    public static String prometheusPort() {
        return PropertiesPrefix.globalPrefix() + PROMETHEUS_PORT;
    }

    private MicrometerProperties() {
        /* This utility class should not be instantiated */
    }
}

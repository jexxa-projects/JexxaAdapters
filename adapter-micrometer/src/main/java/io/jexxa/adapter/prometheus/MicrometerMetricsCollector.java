package io.jexxa.adapter.prometheus;


import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.micrometer.MicrometerPlugin;
import io.javalin.util.JavalinLogger;
import io.jexxa.adapters.api.service.ManagedService;
import io.jexxa.adapters.core.facade.logger.SLF4jLogger;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import static io.jexxa.adapter.prometheus.MicrometerProperties.prometheusEndpoint;
import static io.jexxa.adapter.prometheus.MicrometerProperties.prometheusPort;

public class MicrometerMetricsCollector implements ManagedService {
    private final String prometheusEndpoint;
    private int prometheusPort;
    private Javalin javalin;
    private final PrometheusMeterRegistry meterRegistry;
    private final List<Consumer<MeterRegistry>> consumers = new ArrayList<>();


    MicrometerMetricsCollector(Properties properties) {
        var endpoint = properties.getProperty(prometheusEndpoint(), "/metrics");
        prometheusEndpoint = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        var configuredPort = properties
                .getProperty(prometheusPort(), "8080");
        try {
            prometheusPort = Integer.parseInt(configuredPort);
        } catch (NumberFormatException _) {
            prometheusPort = 8080;
            SLF4jLogger.getLogger(MicrometerMetricsCollector.class).warn(
                    "Invalid value '{}' for property '{}'. Falling back to default port {}.",
                    configuredPort,
                    prometheusPort(),
                    prometheusPort);
        }

        meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    public void registerMetrics(Consumer<MeterRegistry> consumer) { consumers.add(consumer); }

    @Override
    public void start() {
        consumers.forEach(consumer -> consumer.accept(meterRegistry));
        JavalinLogger.startupInfo = false;
        javalin = Javalin
                .create( context -> this.initJavalin(context, meterRegistry));

        javalin.start(prometheusPort);
        SLF4jLogger.getLogger(MicrometerMetricsCollector.class).info("Micrometer     : http://localhost:{}{}", prometheusPort, prometheusEndpoint);
    }

    @Override
    public void stop() {
        javalin.stop();
    }



    private void initJavalin(JavalinConfig config, PrometheusMeterRegistry registry)
    {
        // Plugins registrieren
        config.registerPlugin(new MicrometerPlugin(micrometerConfig -> micrometerConfig.registry = registry));
        config.routes.get(prometheusEndpoint, ctx -> {
            String acceptHeader = ctx.header("Accept");
            if (acceptHeader != null && acceptHeader.contains("application/openmetrics-text")) {
                // OpenMetrics 1.0.0 Format ausgeben
                ctx.contentType("application/openmetrics-text; version=1.0.0; charset=utf-8");
                ctx.result(registry.scrape("application/openmetrics-text"));
            } else {
                // Traditional Prometheus Text-Format 0.0.4 (Fallback)
                ctx.contentType("text/plain; version=0.0.4; charset=utf-8");
                ctx.result(registry.scrape("text/plain"));
            }
        });

    }
}

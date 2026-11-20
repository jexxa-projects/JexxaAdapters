package io.jexxa.adapter.prometheus;


import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.micrometer.MicrometerPlugin;
import io.javalin.util.JavalinLogger;
import io.jexxa.adapters.api.drivingadapter.Diagnostics;
import io.jexxa.adapters.api.service.ManagedService;
import io.jexxa.adapters.core.facade.logger.SLF4jLogger;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.jexxa.adapter.prometheus.MicrometerProperties.prometheusEndpoint;
import static io.jexxa.adapter.prometheus.MicrometerProperties.prometheusPort;


public class MicrometerMetricsCollector implements ManagedService {
    static {
        JavalinLogger.startupInfo = false;
    }

    public static final String JEXXA_HEALTH_STATUS = "jexxa.health.status";
    public static final String JEXXA_HEALTH_LAST_CHECK = "jexxa.health.last.check.seconds";

    private final String prometheusEndpoint;
    private int prometheusPort;
    private Javalin javalin;
    private final PrometheusMeterRegistry meterRegistry;
    private final List<Consumer<MeterRegistry>> consumers = new ArrayList<>();


    public MicrometerMetricsCollector(Properties properties) {
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

    public void register(Consumer<MeterRegistry> consumer) { consumers.add(consumer); }

    @Override
    public void start() {
        consumers.forEach(consumer -> consumer.accept(meterRegistry));
        javalin = Javalin
                .create( context -> this.initJavalin(context, meterRegistry));

        javalin.start(prometheusPort);
        SLF4jLogger.getLogger(MicrometerMetricsCollector.class).info("Micrometer     : http://localhost:{}{}", prometheusPort, prometheusEndpoint);
    }

    @Override
    public void stop() {
        javalin.stop();
    }
    public void register(Supplier<Diagnostics> diagnosticsSupplier) {
        Diagnostics initial = diagnosticsSupplier.get();
        if (initial == null) {
            return;
        }

        // 1-Sekunden-Cache, damit der Supplier für Status- und Timestamp-Gauge nicht doppelt abgefragt wird
        Supplier<Diagnostics> cachedSupplier = memoize(diagnosticsSupplier, Duration.ofSeconds(1));

        Tags tags = Tags.of(
                "check", nullSafe(initial.nameOfHealthCheck()),
                "observed_object", nullSafe(initial.nameOfObservedObject())
        );

        Gauge.builder(JEXXA_HEALTH_STATUS, cachedSupplier, s -> {
                    Diagnostics d = s.get();
                    return (d != null && d.isHealthy()) ? 1.0 : 0.0;
                })
                .description("Health check status (1 = healthy, 0 = unhealthy)")
                .tags(tags)
                .strongReference(true)
                .register(meterRegistry);

        Gauge.builder(JEXXA_HEALTH_LAST_CHECK, cachedSupplier, s -> {
                    Diagnostics d = s.get();
                    return (d != null && d.timestamp() != null) ? (double) d.timestamp().getEpochSecond() : 0.0;
                })
                .description("Timestamp of the last health check execution in epoch seconds")
                .strongReference(true)
                .tags(tags)
                .register(meterRegistry);
    }

    /**
     * Registriert eine Liste von Health-Checks, indem intern an register() delegiert wird.
     */
    public void registerAll(Supplier<List<Diagnostics>> diagnosticsListSupplier) {
        Supplier<List<Diagnostics>> cachedListSupplier = memoize(diagnosticsListSupplier, Duration.ofSeconds(1));
        List<Diagnostics> initialList = cachedListSupplier.get();

        if (initialList == null) {
            return;
        }

        for (Diagnostics diag : initialList) {
            String checkName = diag.nameOfHealthCheck();
            String observedObject = diag.nameOfObservedObject();

            Supplier<Diagnostics> singleSupplier = () -> cachedListSupplier.get().stream()
                    .filter(d -> Objects.equals(checkName, d.nameOfHealthCheck())
                            && Objects.equals(observedObject, d.nameOfObservedObject()))
                    .findFirst()
                    .orElse(null);

            register(singleSupplier);
        }
    }

    private static String nullSafe(String value) {
        return value != null ? value : "unknown";
    }
    // Kleiner Thread-sicherer Scrape-Cache (1 Sekunde)
    private static <T> Supplier<T> memoize(Supplier<T> delegate, Duration ttl) {
        return new Supplier<>() {
            private T cachedValue;
            private Instant expiresAt = Instant.MIN;

            @Override
            public synchronized T get() {
                Instant now = Instant.now();
                if (now.isAfter(expiresAt)) {
                    cachedValue = delegate.get();
                    expiresAt = now.plus(ttl);
                }
                return cachedValue;
            }
        };
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

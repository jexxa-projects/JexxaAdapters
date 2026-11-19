package io.jexxa.adapter.prometheus;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.MeterRegistry;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static io.jexxa.adapter.prometheus.MicrometerProperties.prometheusEndpoint;
import static io.jexxa.adapter.prometheus.MicrometerProperties.prometheusPort;
import static java.lang.Integer.parseInt;
import static org.awaitility.Awaitility.await;

class MicrometerMetricsCollectorTest {
    private static final String MESSAGES_TOTAL = "messages_total";
    private static final String MESSAGES_ORIGIN = "my_component";
    private static final String RESULT_TAG = "result";
    private static final String ERROR_TYPE_TAG = "error_type";
    private final AtomicInteger counter = new AtomicInteger();

    @Test
    void initFlowGraphMetrics()
    {
        //Arrange
        var properties = new Properties();
        properties.setProperty(prometheusPort(), "7070");
        properties.setProperty(prometheusEndpoint(), "/metrics");

        var url = "http://localhost:"
                + properties.getProperty(prometheusPort())
                + properties.getProperty(prometheusEndpoint());

        MicrometerMetricsCollector objectUnderTest = new MicrometerMetricsCollector(properties);
        objectUnderTest.registerMetrics(this::registerMetrics);

        objectUnderTest.start();


        //ACT / ASSERT
        await().atMost(Duration.ofSeconds(5)).until(() -> getProcessedMessages(url) > 0);
    }

    int incrementAndGetCounter() {return counter.incrementAndGet();}


    @AfterEach
    void afterEach()
    {
        Unirest.shutDown();
    }

    private int getProcessedMessages(String url) {
        String rawMetrics = Unirest.get(url).asString().getBody();

        if (rawMetrics == null || rawMetrics.isBlank()) {
            return 0;
        }

        // Zeilenweise verarbeiten - absolut linear, kein Backtracking möglich
         return rawMetrics.lines()
                // 1. Nur die relevante Metrik-Zeile heraussuchen
                .filter(line -> line.startsWith(MESSAGES_TOTAL) && line.contains("result=\"success\""))
                .map(line -> {
                    try {
                        int closingBracketIndex = line.lastIndexOf('}');
                        if (closingBracketIndex == -1) return 0;

                        // Wert nach der Klammer ausschneiden und trimmen (z.B. "6820.0")
                        String valueStr = line.substring(closingBracketIndex + 1).trim();

                        // Das ".0" von Prometheus abschneiden, um es sicher in Integer zu wandeln
                        int dotIndex = valueStr.indexOf('.');
                        if (dotIndex != -1) {
                            valueStr = valueStr.substring(0, dotIndex);
                        }

                        return parseInt(valueStr);
                    } catch (Exception _) {
                        return 0; // Fehlerhafte Zeile ignorieren
                    }
                })
                .findFirst()
                .orElse(0);
    }



    private void registerMetrics(MeterRegistry registry) {
        FunctionCounter.builder(MESSAGES_TOTAL, this, MicrometerMetricsCollectorTest::incrementAndGetCounter)
                .description("Total processed messages")
                .tag(MESSAGES_ORIGIN, this.getClass().getSimpleName())
                .tag(RESULT_TAG, "success")
                .tag(ERROR_TYPE_TAG, "none")
                .register(registry);

        FunctionCounter.builder(MESSAGES_TOTAL, this, MicrometerMetricsCollectorTest::incrementAndGetCounter)
                .description("Total handled error messages")
                .tag(MESSAGES_ORIGIN, this.getClass().getSimpleName())
                .tag(RESULT_TAG, "error")
                .tag(ERROR_TYPE_TAG, "handled")
                .register(registry);

        FunctionCounter.builder(MESSAGES_TOTAL, this, MicrometerMetricsCollectorTest::incrementAndGetCounter)
                .description("Total unhandled error messages")
                .tag(MESSAGES_ORIGIN, this.getClass().getSimpleName())
                .tag(RESULT_TAG, "error")
                .tag(ERROR_TYPE_TAG, "unhandled")
                .register(registry);

    }

}
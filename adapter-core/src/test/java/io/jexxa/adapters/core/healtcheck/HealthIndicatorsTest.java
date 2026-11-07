package io.jexxa.adapters.core.healtcheck;

import io.jexxa.adapters.core.drivingadapter.scheduler.ScheduledFixedRate;
import io.jexxa.adapters.core.drivingadapter.scheduler.Scheduler;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static io.jexxa.adapters.api.invocation.InvocationManager.getRootInterceptor;
import static io.jexxa.adapters.core.healthcheck.HealthIndicators.timeoutIndicator;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HealthIndicatorsTest {
    @Test
    void testTimeoutIndicator()
    {
        //Arrange
        var scheduler = new Scheduler();
        var schedulerListener = new ScheduledFixedRate(() -> {}, 0, 10, TimeUnit.MICROSECONDS);
        scheduler.register(schedulerListener);

        var objectUnderTest = timeoutIndicator(Duration.ofMillis(500));
        var initResult = objectUnderTest.healthy();

        //Act
        getRootInterceptor(schedulerListener).registerBefore(objectUnderTest);

        await()
                .atMost(1, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> !objectUnderTest.healthy());

        // Assert
        assertTrue(initResult);
        assertFalse(objectUnderTest.healthy());
    }
}

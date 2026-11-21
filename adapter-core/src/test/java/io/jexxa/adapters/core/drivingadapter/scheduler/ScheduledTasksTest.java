package io.jexxa.adapters.core.drivingadapter.scheduler;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScheduledTasksTest {

    @Test
    void testScheduledFixedDelayConfigurationAndExecution() {
        // Arrange
        var counter = new AtomicInteger(0);
        var task = new ScheduledFixedDelay(counter::incrementAndGet, 10, 50, TimeUnit.SECONDS);

        // Act
        task.execute();

        // Assert
        assertEquals(10, task.initialDelay());
        assertEquals(50, task.fixedDelay());
        assertEquals(-1, task.fixedRate()); // Standardwert aus IScheduled
        assertEquals(TimeUnit.SECONDS, task.timeUnit());
        assertEquals(1, counter.get());
    }

    @Test
    void testScheduledFixedRateConfigurationAndExecution() {
        // Arrange
        var counter = new AtomicInteger(0);
        var task = new ScheduledFixedRate(counter::incrementAndGet, 5, 100, TimeUnit.MILLISECONDS);

        // Act
        task.execute();

        // Assert
        assertEquals(5, task.initialDelay());
        assertEquals(100, task.fixedRate());
        assertEquals(-1, task.fixedDelay()); // Standardwert aus IScheduled
        assertEquals(TimeUnit.MILLISECONDS, task.timeUnit());
        assertEquals(1, counter.get());
    }

    @Test
    void testRepeatedFixedDelayStopsAfterMaxRepeat() {
        // Arrange
        var counter = new AtomicInteger(0);
        long maxRepeat = 3;
        var task = new RepeatedFixedDelay(maxRepeat, counter::incrementAndGet, 0, 10, TimeUnit.MILLISECONDS);

        // Act - 5-mal ausführen, obwohl maxRepeat = 3 ist
        for (int i = 0; i < 5; i++) {
            task.execute();
        }

        // Assert
        assertEquals(maxRepeat, counter.get(), "Command darf exakt maxRepeat-mal ausgeführt werden");
        assertEquals(0, task.initialDelay());
        assertEquals(10, task.fixedDelay());
        assertEquals(TimeUnit.MILLISECONDS, task.timeUnit());
    }

    @Test
    void testRepeatedFixedRateStopsAfterMaxRepeat() {
        // Arrange
        var counter = new AtomicInteger(0);
        long maxRepeat = 2;
        var task = new RepeatedFixedRate(maxRepeat, counter::incrementAndGet, 1, 20, TimeUnit.MINUTES);

        // Act - 4-mal ausführen, obwohl maxRepeat = 2 ist
        for (int i = 0; i < 4; i++) {
            task.execute();
        }

        // Assert
        assertEquals(maxRepeat, counter.get(), "Command darf exakt maxRepeat-mal ausgeführt werden");
        assertEquals(1, task.initialDelay());
        assertEquals(20, task.fixedRate());
        assertEquals(TimeUnit.MINUTES, task.timeUnit());
    }

    @Test
    void testRepeatedTaskWithZeroRepeatsNeverExecutes() {
        // Arrange
        var counter = new AtomicInteger(0);
        var task = new RepeatedFixedRate(0, counter::incrementAndGet, 0, 10, TimeUnit.SECONDS);

        // Act
        task.execute();

        // Assert
        assertEquals(0, counter.get());
    }
}

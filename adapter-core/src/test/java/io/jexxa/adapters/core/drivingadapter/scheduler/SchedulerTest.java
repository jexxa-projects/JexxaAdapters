package io.jexxa.adapters.core.drivingadapter.scheduler;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchedulerTest {

    private Scheduler objectUnderTest;

    @BeforeEach
    void setUp() {
        objectUnderTest = new Scheduler();
    }

    @AfterEach
    void tearDown() {
        if (objectUnderTest != null) {
            objectUnderTest.stop();
        }
    }

    @Test
    void testScheduleAnnotatedFixedRateMethod() throws InterruptedException {
        // Arrange
        var latch = new CountDownLatch(3);
        var testPort = new ValidFixedRatePort(latch);

        // Act
        objectUnderTest.register(testPort);
        objectUnderTest.start();

        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Methode sollte mindestens 3-mal ausgeführt werden");
    }

    @Test
    void testScheduleAnnotatedFixedDelayMethod() throws InterruptedException {
        // Arrange
        var latch = new CountDownLatch(3);
        var testPort = new ValidFixedDelayPort(latch);

        // Act
        objectUnderTest.register(testPort);
        objectUnderTest.start();

        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Methode sollte mindestens 3-mal ausgeführt werden");
    }

    @Test
    void testScheduleIScheduledObject() throws InterruptedException {
        // Arrange
        var latch = new CountDownLatch(2);
        var scheduledTask = new ScheduledFixedRate(latch::countDown, 0, 20, TimeUnit.MILLISECONDS);

        // Act
        objectUnderTest.register(scheduledTask);
        objectUnderTest.start();

        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS), "IScheduled-Objekt sollte ausgeführt werden");
    }

    @Test
    void testRegisterObjectWithoutScheduledAnnotationThrowsException() {
        // Arrange
        var invalidPort = new Object();

        // Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> objectUnderTest.register(invalidPort));
        assertTrue(exception.getMessage().contains("does not provide a Scheduled annotation"));
    }

    @Test
    void testRegisterMethodWithBothFixedRateAndFixedDelayThrowsException() {
        // Arrange
        var invalidPort = new BothIntervalsSetPort();

        // Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> objectUnderTest.register(invalidPort));
        assertTrue(exception.getMessage().contains("exact one of these values must be >=0"));
    }

    @Test
    void testRegisterMethodWithNeitherFixedRateNorFixedDelayThrowsException() {
        // Arrange
        var invalidPort = new NoIntervalSetPort();

        // Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> objectUnderTest.register(invalidPort));
        assertTrue(exception.getMessage().contains("exact one of these values must be >=0"));
    }

    @Test
    void testExceptionInScheduledMethodDoesNotStopScheduler() throws InterruptedException {
        // Arrange
        var counter = new AtomicInteger(0);
        var latch = new CountDownLatch(2);

        IScheduled failingTask = new ScheduledFixedRate(() -> {
            counter.incrementAndGet();
            latch.countDown();
            throw new RuntimeException("Simulierter Fehler im Task");
        }, 0, 20, TimeUnit.MILLISECONDS);

        // Act
        objectUnderTest.register(failingTask);
        objectUnderTest.start();

        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Scheduler muss trotz Exceptions weiterlaufen");
        assertTrue(counter.get() >= 2);
    }

    @Test
    void testStopSchedulerGracefully() {
        // Arrange
        var scheduledTask = new ScheduledFixedRate(() -> {}, 0, 50, TimeUnit.MILLISECONDS);
        objectUnderTest.register(scheduledTask);
        objectUnderTest.start();

        // Act & Assert
        assertDoesNotThrow(() -> objectUnderTest.stop());
    }

    // --- Hilfsklassen für die Tests ---

    public static class ValidFixedRatePort {
        private final CountDownLatch latch;

        public ValidFixedRatePort(CountDownLatch latch) {
            this.latch = latch;
        }

        @SuppressWarnings("unused")
        @Scheduled(fixedRate = 20, timeUnit = TimeUnit.MILLISECONDS)
        public void runTask() {
            latch.countDown();
        }
    }

    public static class ValidFixedDelayPort {
        private final CountDownLatch latch;

        public ValidFixedDelayPort(CountDownLatch latch) {
            this.latch = latch;
        }

        @SuppressWarnings("unused")
        @Scheduled(fixedDelay = 20, timeUnit = TimeUnit.MILLISECONDS)
        public void runTask() {
            latch.countDown();
        }
    }

    public static class BothIntervalsSetPort {
        @SuppressWarnings("unused")
        @Scheduled(fixedRate = 100, fixedDelay = 100)
        public void invalidTask() {
            // Darf nicht registriert werden
        }
    }

    public static class NoIntervalSetPort {
        @SuppressWarnings("unused")
        @Scheduled()
        public void invalidTask() {
            // Darf nicht registriert werden
        }
    }
}

package io.jexxa.common.drivingadapter.scheduler;

import io.jexxa.common.drivingadapter.scheduler.portadapter.FixedDelayIncrementer;
import io.jexxa.common.drivingadapter.scheduler.portadapter.FixedRateIncrementer;
import io.jexxa.common.drivingadapter.scheduler.portadapter.InvalidScheduledAnnotation;
import io.jexxa.common.drivingadapter.scheduler.portadapter.MissingScheduledAnnotation;
import io.jexxa.common.drivingadapter.scheduler.portadapter.MultipleIncrementer;
import io.jexxa.common.drivingadapter.scheduler.portadapter.ThrowingIncrementer;
import io.jexxa.common.facade.testapplication.SimpleApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SchedulerTest {
    private Scheduler objectUnderTest;
    private SimpleApplicationService application;
    private static final AtomicInteger scheduledCounter = new AtomicInteger();
    @BeforeEach
    void initBeforeEach()
    {
        objectUnderTest = new Scheduler();
        application = new SimpleApplicationService();
        scheduledCounter.set(0);
    }

    @Test
    void testFixedRateScheduler()
    {
        //Arrange
        var fixedRateIncrementer = new FixedRateIncrementer(application);
        objectUnderTest.register(fixedRateIncrementer);

        //Act
        objectUnderTest.start();

        //Assert that simple value is incremented > 100 within 5 seconds
        await()
                .atMost(5, TimeUnit.SECONDS)
                .pollDelay(50, MILLISECONDS)
                .until(() -> application.getSimpleValue() > 100);

        objectUnderTest.stop();
    }

    @Test
    void testFixedDelayScheduler()
    {
        //Arrange
        var fixedDelayIncrementer = new FixedDelayIncrementer(application);
        objectUnderTest.register(fixedDelayIncrementer);

        //Act
        objectUnderTest.start();

        //Assert
        await()
                .atMost(5, TimeUnit.SECONDS)
                .pollDelay(50, MILLISECONDS)
                .until(() -> application.getSimpleValue() > 100);

        objectUnderTest.stop();
    }

    @ParameterizedTest
    @MethodSource("getScheduledStrategies")
     void testScheduledStrategies(IScheduled iScheduled)
     {
         //Arrange
         objectUnderTest.register(iScheduled);

         //Act
         objectUnderTest.start();

         //Assert
         await()
                 .atMost(5, TimeUnit.SECONDS)
                 .pollDelay(50, MILLISECONDS)
                 .until(() -> scheduledCounter.get() > 100);

         objectUnderTest.stop();
     }

     static Stream<IScheduled> getScheduledStrategies()
     {
         return Stream.of(new ScheduledFixedRate(scheduledCounter::incrementAndGet,  0, 5 , MICROSECONDS),
                 new ScheduledFixedDelay(scheduledCounter::incrementAndGet,  0, 5 , MICROSECONDS));
     }


    @ParameterizedTest
    @MethodSource("getRepeatedStrategies")
    void testRepeatedStrategies(IScheduled iScheduled)
    {
        //Arrange
        objectUnderTest.register(iScheduled);

        //Act
        objectUnderTest.start();

        //Assert
        await()
                .atMost(5, TimeUnit.SECONDS)
                .pollDelay(50, MILLISECONDS)
                .until(() -> scheduledCounter.get() == 100);

        objectUnderTest.stop();

        assertEquals(100, scheduledCounter.get());
    }

    static Stream<IScheduled> getRepeatedStrategies()
    {
        return Stream.of(new RepeatedFixedDelay(100, scheduledCounter::incrementAndGet,  0, 5 , MICROSECONDS),
                new RepeatedFixedRate(100, scheduledCounter::incrementAndGet,  0, 5 , MICROSECONDS));
    }


    @Test
    void testMultipleScheduler()
    {
        //Arrange
        var fixedDelayIncrementer = new MultipleIncrementer(application);
        objectUnderTest.register(fixedDelayIncrementer);

        //Act
        objectUnderTest.start();

        //Assert that both values are incremented
        await()
                .atMost(5, TimeUnit.SECONDS)
                .pollDelay(50, MILLISECONDS)
                .until(() -> application.getSimpleValue() > 100
                && application.getSimpleValueObject().getValue() > 100);

        objectUnderTest.stop();
    }

    @Test
    void testMissingScheduledAnnotation()
    {
        //Arrange
        var missingAnnotation = new MissingScheduledAnnotation(application);

        //Act / Assert
        assertThrows( IllegalArgumentException.class,  () -> objectUnderTest.register(missingAnnotation));
    }

    @Test
    void testInvalidScheduledAnnotation()
    {
        //Arrange
        var invalidScheduledAnnotation = new InvalidScheduledAnnotation(application);


        //Act / Assert
        assertThrows( IllegalArgumentException.class,  () -> objectUnderTest.register(invalidScheduledAnnotation));
    }

    @Test
    void testThrowingScheduler()
    {
        //Arrange
        var throwingIncrementer = new ThrowingIncrementer(application);
        objectUnderTest.register(throwingIncrementer);

        //Act
        objectUnderTest.start();

        //Assert that simple value is incremented > 100 within 5 seconds
        await()
                .atMost(5, TimeUnit.SECONDS)
                .pollDelay(50, MILLISECONDS)
                .until(() -> application.getSimpleValue() > 100);

        objectUnderTest.stop();
    }
}

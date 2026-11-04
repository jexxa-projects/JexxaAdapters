package io.jexxa.common.drivingadapter.scheduler.portadapter;


import io.jexxa.core.drivingadapter.scheduler.Scheduled;
import io.jexxa.common.testapplication.SimpleApplicationService;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ThrowingIncrementer {
    private final SimpleApplicationService simpleApplicationService;

    public ThrowingIncrementer(SimpleApplicationService simpleApplicationService) {
        this.simpleApplicationService = simpleApplicationService;
    }

    @Scheduled(fixedRate = 10, timeUnit = MILLISECONDS)
    @SuppressWarnings("unused")
    public void throwingIncrementCounter() {
        simpleApplicationService.setSimpleValue(simpleApplicationService.getSimpleValue() + 1);
        throw new RuntimeException("Test for handling Exceptions in DrivingAdapter");
    }
}
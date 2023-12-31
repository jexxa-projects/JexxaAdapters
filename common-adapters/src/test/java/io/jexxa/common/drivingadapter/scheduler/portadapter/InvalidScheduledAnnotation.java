package io.jexxa.common.drivingadapter.scheduler.portadapter;


import io.jexxa.common.drivingadapter.scheduler.Scheduled;
import io.jexxa.common.facade.testapplication.SimpleApplicationService;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class InvalidScheduledAnnotation {
    private final SimpleApplicationService simpleApplicationService;

    public InvalidScheduledAnnotation(SimpleApplicationService simpleApplicationService) {
        this.simpleApplicationService = simpleApplicationService;
    }

    @SuppressWarnings("unused")
    @Scheduled(timeUnit = MILLISECONDS)
    public void run() {
        simpleApplicationService.setSimpleValue(simpleApplicationService.getSimpleValue() + 1);
    }
}

package io.jexxa.common.drivingadapter.scheduler.portadapter;

import io.jexxa.common.drivingadapter.scheduler.Scheduled;
import io.jexxa.common.facade.testapplication.SimpleApplicationService;
import io.jexxa.common.facade.testapplication.TestValueObject;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class MultipleIncrementer {
    private final SimpleApplicationService simpleApplicationService;

    public MultipleIncrementer(SimpleApplicationService simpleApplicationService) {
        this.simpleApplicationService = simpleApplicationService;
    }

    @Scheduled(fixedRate = 10, timeUnit = MILLISECONDS)
    @SuppressWarnings("unused")
    public void incrementCounter() {
        simpleApplicationService.setSimpleValue(simpleApplicationService.getSimpleValue() + 1);
    }

    @Scheduled(fixedDelay = 10, timeUnit = MILLISECONDS)
    @SuppressWarnings("unused")
    public void incrementValueObject() {
        simpleApplicationService.setSimpleValueObject(new TestValueObject(simpleApplicationService.getSimpleValueObject().getValue()+1));
    }

}

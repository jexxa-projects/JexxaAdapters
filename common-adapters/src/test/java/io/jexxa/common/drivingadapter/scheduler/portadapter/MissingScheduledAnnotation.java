package io.jexxa.common.drivingadapter.scheduler.portadapter;


import io.jexxa.common.facade.testapplication.SimpleApplicationService;

public class MissingScheduledAnnotation {
    private final SimpleApplicationService simpleApplicationService;

    public MissingScheduledAnnotation(SimpleApplicationService simpleApplicationService) {
        this.simpleApplicationService = simpleApplicationService;
    }

    @SuppressWarnings("unused")
    public void run() {
        simpleApplicationService.setSimpleValue(simpleApplicationService.getSimpleValue() + 1);
    }
}

package io.jexxa.common.drivingadapter.scheduler;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class ScheduledFixedRate implements IScheduled {
    private final Runnable command;
    private final long initialDelay;
    private final long period;
    private final TimeUnit timeUnit;

    public ScheduledFixedRate(Runnable command,
                              long initialDelay,
                              long period,
                              TimeUnit timeUnit)
    {
        this.command = command;
        this.initialDelay = initialDelay;
        this.period = period;
        this.timeUnit = timeUnit;
    }


    @Override
    public long fixedRate() {
        return period;
    }

    @Override
    public TimeUnit timeUnit() {
        return timeUnit;
    }

    @Override
    public long initialDelay() {
        return initialDelay;
    }

    @Override
    public void execute() {
        command.run();
    }
}

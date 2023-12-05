package io.jexxa.common.drivingadapter.scheduler;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class ScheduledFixedDelay implements IScheduled {
    private final Runnable command;
    private final long initialDelay;
    private final long delay;
    private final TimeUnit timeUnit;

    public ScheduledFixedDelay(Runnable command,
                               long initialDelay,
                               long delay,
                               TimeUnit timeUnit)
    {
        this.command = command;
        this.initialDelay = initialDelay;
        this.delay = delay;
        this.timeUnit = timeUnit;
    }

    @Override
    public long fixedDelay() {return delay;}

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

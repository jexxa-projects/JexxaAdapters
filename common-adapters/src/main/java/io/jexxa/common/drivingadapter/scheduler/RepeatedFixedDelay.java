package io.jexxa.common.drivingadapter.scheduler;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class RepeatedFixedDelay extends ScheduledFixedDelay{
    private final long maxRepeat;
    private long repeatCounter;

    public RepeatedFixedDelay(long repeat, Runnable command,
                              long initialDelay,
                              long delay,
                              TimeUnit timeUnit)
    {
        super(command, initialDelay, delay, timeUnit);
        this.maxRepeat = repeat;
    }

    @Override
    public void execute()
    {
        if (repeatCounter < maxRepeat)
        {
            super.execute();
            ++repeatCounter;
        }
    }
}

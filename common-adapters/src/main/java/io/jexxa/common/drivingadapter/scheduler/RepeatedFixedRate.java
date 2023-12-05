package io.jexxa.common.drivingadapter.scheduler;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class RepeatedFixedRate extends ScheduledFixedRate {
    private final long maxRepeat;
    private long repeatCounter;

    public RepeatedFixedRate(long repeat, Runnable command,
                             long initialDelay,
                             long period,
                             TimeUnit timeUnit)
    {
        super(command, initialDelay, period, timeUnit);
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

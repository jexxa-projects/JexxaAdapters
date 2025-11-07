package io.jexxa.common.healthcheck;

import java.time.Duration;

public final class HealthIndicators
{
    public static TimeoutIndicator timeoutIndicator(Duration maxTimeout )
    {
        return new TimeoutIndicator(maxTimeout);
    }

    private HealthIndicators()
    {
        //Private constructors
    }
}

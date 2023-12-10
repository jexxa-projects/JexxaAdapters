package io.jexxa.common.healthcheck;

import java.time.Duration;

public final class HealthIndicators
{
    public static TimoutIndicator timeoutIndicator(Duration maxTimeout )
    {
        return new TimoutIndicator(maxTimeout);
    }

    private HealthIndicators()
    {
        //Private constructors
    }
}

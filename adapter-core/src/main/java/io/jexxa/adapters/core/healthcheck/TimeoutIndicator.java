package io.jexxa.adapters.core.healthcheck;


import io.jexxa.adapters.api.invocation.InvocationContext;
import io.jexxa.adapters.api.invocation.monitor.BeforeMonitor;

import java.time.Duration;
import java.time.Instant;

public class TimeoutIndicator extends BeforeMonitor
{
    private Instant lastUpdate = Instant.now();
    private final Duration messageTimeout;

    public TimeoutIndicator(Duration messageTimeout)
    {
        this.messageTimeout = messageTimeout;
    }

    @Override
    public void before(InvocationContext invocationContext)
    {
        lastUpdate = Instant.now();
    }

    @Override
    public boolean healthy()
    {
        return Duration.between(lastUpdate, Instant.now()).toMillis() <= messageTimeout.toMillis();
    }

    @Override
    public String getStatusMessage()
    {
        if (!healthy() && getObservedObject() != null)
        {
            String objectName = "Unknown";
            if (getObservedObject() != null )
            {
                objectName = getObservedObject().getClass().getSimpleName();
            }

            return "Did not receive a message for object "
                    + objectName
                    + " within period of "
                    + Duration.between(lastUpdate, Instant.now()).toSeconds()
                    + " seconds.";
        }

        return "Received last message before "
                + Duration.between(lastUpdate, Instant.now()).toSeconds()
                +" seconds. All fine...";
    }
}
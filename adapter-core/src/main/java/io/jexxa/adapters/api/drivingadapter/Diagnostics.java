package io.jexxa.adapters.api.drivingadapter;

import java.time.Instant;

public record Diagnostics(String nameOfHealthCheck,
                          String nameOfObservedObject,
                          boolean isHealthy,
                          String statusMessage,
                          Instant timestamp)
{

}
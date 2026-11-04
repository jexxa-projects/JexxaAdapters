package io.jexxa.adapterapi;

import java.io.Serial;

/**
 * Thrown when application configuration validation fails.
 * <p>
 * This exception indicates a critical misconfiguration that prevents the application
 * from starting correctly. It is a runtime exception on purpose, as such errors
 * should terminate the application immediately rather than being recovered from.
 */
@SuppressWarnings("unused")
public class ConfigurationFailedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    
    public ConfigurationFailedException(String message) {
        super(message);
    }

    public ConfigurationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
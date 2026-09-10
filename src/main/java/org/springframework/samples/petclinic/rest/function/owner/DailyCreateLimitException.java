package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link EnsureDailyCreateLimit} when the maximum number of owners has
 * already been created today (by registrationDate). Handled by
 * {@code DailyCreateLimitExceptionHandler}, which responds 429.
 */
public class DailyCreateLimitException extends Exception {

    public DailyCreateLimitException(int limit) {
        super("The daily limit of " + limit + " owner registrations has already been reached");
    }
}

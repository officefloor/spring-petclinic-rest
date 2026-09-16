package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request arrives after the maximum number of owners (100) have
 * already been registered on the current date. Handled by
 * {@link DailyRegistrationLimitExceptionHandler}, which responds 429.
 */
public class DailyRegistrationLimitException extends Exception {

    public DailyRegistrationLimitException(int limit) {
        super("The maximum number of owners for today has already been reached (" + limit + ").");
    }
}

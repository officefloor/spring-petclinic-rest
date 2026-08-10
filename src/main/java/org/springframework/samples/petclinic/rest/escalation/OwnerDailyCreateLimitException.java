package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner would exceed the per-day create limit: 100 or more owners have
 * already been registered today (by registrationDate). Handled globally by
 * {@link OwnerDailyCreateLimitExceptionHandler}, which responds 429.
 */
public class OwnerDailyCreateLimitException extends Exception {

    public OwnerDailyCreateLimitException(String message) {
        super(message);
    }
}

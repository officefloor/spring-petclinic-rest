package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner once 100 or more owners have already been created
 * today (compared by {@code registrationDate}). Handled globally by
 * {@link OwnerDailyLimitExceptionHandler}, which responds 429 Too Many Requests.
 */
public class OwnerDailyLimitException extends Exception {

    public OwnerDailyLimitException(String message) {
        super(message);
    }
}

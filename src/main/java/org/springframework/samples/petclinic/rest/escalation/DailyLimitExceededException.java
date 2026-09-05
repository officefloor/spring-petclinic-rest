package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request arrives after 100 or more owners have already been
 * registered today (by {@code registrationDate}). Handled by
 * {@link DailyLimitExceededHandler}, which responds 429.
 */
public class DailyLimitExceededException extends Exception {

    public DailyLimitExceededException(String message) {
        super(message);
    }
}

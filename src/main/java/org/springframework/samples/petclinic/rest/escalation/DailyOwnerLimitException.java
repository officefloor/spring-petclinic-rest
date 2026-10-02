package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the owner-create pipeline when 100 or more owners have already been created
 * today (by {@code registrationDate}), so the daily create limit has been reached.
 *
 * <p>Handled by {@link DailyOwnerLimitExceptionHandler}, which responds 429.
 */
public class DailyOwnerLimitException extends Exception {

    public DailyOwnerLimitException(String message) {
        super(message);
    }
}

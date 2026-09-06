package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.EnsureDailyOwnerLimit}
 * when 100 or more owners have already been created today (by registrationDate). Handled by
 * {@link DailyOwnerLimitExceptionHandler}, which responds 429.
 */
public class DailyOwnerLimitException extends Exception {

    public DailyOwnerLimitException(String message) {
        super(message);
    }
}

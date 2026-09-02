package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when the maximum number of owners has already been created today (by
 * registrationDate). Handled by {@link DailyOwnerLimitExceptionHandler}, which responds 429.
 */
public class DailyOwnerLimitException extends Exception {

    public DailyOwnerLimitException(long count) {
        super("Daily owner creation limit reached: " + count);
    }
}

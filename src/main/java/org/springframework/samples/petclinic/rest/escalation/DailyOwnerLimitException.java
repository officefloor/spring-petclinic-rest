package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request arrives after the maximum number of owners for the
 * current day has already been reached (counted by registration date). Handled by
 * {@link DailyOwnerLimitExceptionHandler} as a 429.
 */
public class DailyOwnerLimitException extends Exception {

    public DailyOwnerLimitException(int limit) {
        super("Daily owner creation limit reached (" + limit + " owners today)");
    }
}

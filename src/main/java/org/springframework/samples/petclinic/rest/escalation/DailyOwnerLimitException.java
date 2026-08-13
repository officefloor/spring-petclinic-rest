package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown on create when the maximum number of owners ({@code 100}) have already been
 * registered today (by {@code registrationDate}). Handled by
 * {@link DailyOwnerLimitExceptionHandler}, which responds 429.
 */
public class DailyOwnerLimitException extends Exception {

    public DailyOwnerLimitException(int limit) {
        super("Daily owner registration limit reached (" + limit + " owners today)");
    }
}

package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckDailyOwnerLimit} when {@value #DAILY_LIMIT} or more owners have already
 * been created for the request's business day (compared by {@code registrationDate}).
 * Handled by {@link DailyOwnerLimitExceptionHandler}, which responds 429 (Too Many Requests).
 */
public class DailyOwnerLimitException extends Exception {

    /** Maximum owners permitted to be created in a single day. */
    public static final int DAILY_LIMIT = 100;

    public DailyOwnerLimitException(int limit) {
        super("The daily limit of " + limit
                + " new owners has already been reached; try again tomorrow.");
    }
}

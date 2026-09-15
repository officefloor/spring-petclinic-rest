package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request arrives after the maximum number of owners has
 * already been registered on the server's current date. Handled by
 * {@link DailyLimitExceededExceptionHandler}, which responds 429.
 */
public class DailyLimitExceededException extends Exception {

    private final long limit;

    public DailyLimitExceededException(long limit) {
        super("Daily owner registration limit reached (" + limit + " per day)");
        this.limit = limit;
    }

    public long getLimit() {
        return this.limit;
    }
}

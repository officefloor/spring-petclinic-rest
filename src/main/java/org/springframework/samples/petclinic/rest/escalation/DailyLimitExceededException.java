package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a request to create an owner would exceed the per-day create limit: the maximum number
 * of owners registered today (by {@code registrationDate}) has already been reached, so the request is
 * answered with a 429 Too Many Requests rather than exceeding the daily cap. Carries the limit so
 * {@link DailyLimitExceededExceptionHandler} can report it. Checked so it appears in the function's
 * {@code throws} clause and is routed as an escalation.
 */
public class DailyLimitExceededException extends Exception {

    private final int limit;

    public DailyLimitExceededException(int limit) {
        super("The maximum of " + limit + " owners have already been created today");
        this.limit = limit;
    }

    public int getLimit() {
        return this.limit;
    }
}

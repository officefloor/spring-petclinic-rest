package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request would exceed the per-day cap: the number of owners already registered
 * today has reached the daily limit. Carries that limit so {@link DailyOwnerLimitExceptionHandler} can
 * report it. Handled as a 429.
 */
public class DailyOwnerLimitException extends Exception {

    private final int limit;

    public DailyOwnerLimitException(int limit) {
        super("Daily owner registration limit reached (" + limit + ")");
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }
}

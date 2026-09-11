package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request would exceed the daily cap: 100 or more owners have
 * already been created today (by registration date). Handled by
 * {@link OwnerDailyLimitExceededExceptionHandler}, which responds 429 Too Many Requests.
 */
public class OwnerDailyLimitExceededException extends Exception {

    private final int count;

    public OwnerDailyLimitExceededException(int count) {
        super("Daily owner creation limit reached: " + count + " owners already created today");
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}

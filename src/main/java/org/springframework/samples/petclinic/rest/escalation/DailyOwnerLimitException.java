package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request would exceed the maximum number of owners that may be created
 * in a single day (100), counted by {@code registrationDate}. Handled by
 * {@link DailyOwnerLimitExceptionHandler}, which responds 429 (Too Many Requests).
 */
public class DailyOwnerLimitException extends Exception {

    private final int count;

    public DailyOwnerLimitException(int count) {
        super("Daily owner creation limit reached: " + count + " owners already created today (limit 100)");
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }
}

package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code EnsureDailyOwnerLimit} when a create-owner request arrives after 100 or more
 * owners have already been registered today (by registrationDate). Handled by
 * {@link DailyOwnerLimitExceptionHandler}, which responds 429 so no further owners can be created
 * today once the daily limit is reached.
 */
public class DailyOwnerLimitException extends Exception {

    private final int count;

    public DailyOwnerLimitException(int count) {
        super("Daily owner limit reached: " + count + " owners already created today (limit is 100)");
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }
}

package org.springframework.samples.petclinic.rest.escalation;

/**
 * Raised when a create-owner request arrives after the maximum number of owners (100) have already
 * been created today, counted by {@code registrationDate}. Checked so it appears in a
 * {@code throws} clause and can be routed to its escalation handler, which responds 429 Too Many
 * Requests.
 */
public class DailyOwnerLimitException extends Exception {

    private final int count;

    public DailyOwnerLimitException(int count) {
        super("Daily owner creation limit reached: " + count);
        this.count = count;
    }

    /** The number of owners already created today. */
    public int getCount() {
        return this.count;
    }
}

package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code EnsureDailyLimit} when 100 or more owners have already been registered today (by
 * {@code registrationDate}). Handled globally by {@link DailyLimitReachedExceptionHandler}, which
 * responds 429.
 */
public class DailyLimitReachedException extends Exception {

    private final int count;

    public DailyLimitReachedException(int count) {
        super("Daily owner registration limit reached (" + count + " owners registered today)");
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }
}

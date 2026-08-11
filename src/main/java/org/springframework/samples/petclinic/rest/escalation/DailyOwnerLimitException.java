package org.springframework.samples.petclinic.rest.escalation;

/**
 * Raised when a create-owner request arrives after {@link
 * org.springframework.samples.petclinic.rest.function.owner.CheckOwnerDailyLimit#DAILY_LIMIT}
 * or more owners have already been registered today (compared by {@code registrationDate}).
 * Carries the current count so the handler can report how many were already created today.
 */
public class DailyOwnerLimitException extends Exception {

    private final int count;

    public DailyOwnerLimitException(int count) {
        super(count + " owners have already been created today; the daily limit has been reached");
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}

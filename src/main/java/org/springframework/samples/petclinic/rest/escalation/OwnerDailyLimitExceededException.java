package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown by the create-owner pipeline when 100 or more owners have already been created today
 * (counted by {@code registrationDate} equal to the current date). The day's quota is full, so
 * no further owner may be created until tomorrow.
 *
 * <p>Carries the day and its count so {@link OwnerDailyLimitExceededExceptionHandler} can respond
 * 429 explaining why the create was rejected.
 */
public class OwnerDailyLimitExceededException extends Exception {

    private final LocalDate date;

    private final long count;

    public OwnerDailyLimitExceededException(LocalDate date, long count) {
        super("Already created " + count + " owners on " + date + "; the daily limit is 100");
        this.date = date;
        this.count = count;
    }

    public LocalDate getDate() {
        return date;
    }

    public long getCount() {
        return count;
    }
}

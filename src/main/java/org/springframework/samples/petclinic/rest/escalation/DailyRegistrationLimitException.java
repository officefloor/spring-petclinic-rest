package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when creating an owner once 100 or more owners have already been registered
 * today (by registration date). Handled globally by
 * {@link DailyRegistrationLimitExceptionHandler}, which responds 429.
 */
public class DailyRegistrationLimitException extends Exception {

    private final LocalDate date;

    private final int count;

    public DailyRegistrationLimitException(LocalDate date, int count) {
        super("The daily registration limit has been reached: " + count
                + " owners were already registered on " + date);
        this.date = date;
        this.count = count;
    }

    /** The registration date that has reached its limit. */
    public LocalDate getDate() {
        return this.date;
    }

    /** The number of owners already registered on that date. */
    public int getCount() {
        return this.count;
    }
}

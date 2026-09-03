package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when a create-owner request arrives after the maximum number of owners (100) have already
 * been created today, counted by registration date. Handled globally by
 * {@link DailyOwnerLimitExceptionHandler}, which responds 429.
 */
public class DailyOwnerLimitException extends Exception {

    /** Once this many owners have been created on a day, the next one is rejected. */
    public static final int LIMIT = 100;

    private final LocalDate date;

    private final int count;

    public DailyOwnerLimitException(LocalDate date, int count) {
        super("Already created " + count + " owners on " + date + " (maximum " + LIMIT + " per day)");
        this.date = date;
        this.count = count;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public int getCount() {
        return this.count;
    }
}

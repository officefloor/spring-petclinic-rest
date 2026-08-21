package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when a create-owner request arrives after 100 or more owners have already been registered
 * today (compared by {@code registrationDate}). Handled globally by
 * {@link DailyOwnerLimitExceptionHandler}, which responds 429.
 */
public class DailyOwnerLimitException extends Exception {

    private final LocalDate date;

    private final int count;

    public DailyOwnerLimitException(LocalDate date, int count) {
        super("Daily owner limit reached: " + count + " owners already registered on " + date);
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

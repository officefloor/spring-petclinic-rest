package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when a create-owner request arrives after the maximum number of owners have
 * already been registered on the current day, so no more may be created today. Handled by
 * {@link DailyLimitExceededExceptionHandler}, which responds 429.
 */
public class DailyLimitExceededException extends Exception {

    private final LocalDate date;

    private final int limit;

    public DailyLimitExceededException(LocalDate date, int limit) {
        super("Daily owner limit reached (" + limit + " owners) for " + date);
        this.date = date;
        this.limit = limit;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public int getLimit() {
        return this.limit;
    }
}

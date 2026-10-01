package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.CheckOwnerDailyLimit}
 * when the maximum number of owners (100) have already been created today, as determined by their
 * {@code registrationDate}. Handled by {@link OwnerDailyLimitExceptionHandler}, which responds 429.
 */
public class OwnerDailyLimitException extends Exception {

    private final LocalDate date;

    private final int count;

    public OwnerDailyLimitException(LocalDate date, int count) {
        super("The maximum number of owners has already been created today: " + date + " (" + count + ")");
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

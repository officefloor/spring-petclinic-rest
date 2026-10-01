package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * Thrown by {@link CheckOwnerDailyLimit} when 100 or more owners already carry the current day's
 * registration date. Handled globally by {@code OwnerDailyLimitReachedExceptionHandler}, which
 * responds 429 Too Many Requests.
 */
public class OwnerDailyLimitReachedException extends Exception {

    private final LocalDate date;

    public OwnerDailyLimitReachedException(LocalDate date) {
        super("Daily owner creation limit reached for: " + date);
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }
}

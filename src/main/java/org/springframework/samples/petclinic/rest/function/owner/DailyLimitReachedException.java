package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * Thrown by {@link EnsureDailyLimit} when the maximum number of owners for the current day has
 * already been created. Carries the day for the handler's message. Checked so it appears in the
 * function's {@code throws} clause and routes to an escalation.
 */
public class DailyLimitReachedException extends Exception {

    private final LocalDate date;

    public DailyLimitReachedException(LocalDate date) {
        super("Daily owner creation limit reached for: " + date);
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }
}

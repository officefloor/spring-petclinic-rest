package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown by the create-owner pipeline when the current day has already reached the maximum
 * number of owner registrations (the per-day limit), so no further owner may be registered
 * today. Carries the day so {@link DailyOwnerLimitExceptionHandler} can report it. Handled as
 * 429 Too Many Requests.
 */
public class DailyOwnerLimitException extends Exception {

    private final LocalDate date;

    public DailyOwnerLimitException(LocalDate date) {
        super("Daily owner registration limit reached for: " + date);
        this.date = date;
    }

    public LocalDate getDate() {
        return this.date;
    }
}

package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when a create-owner request would exceed the maximum number of owners that may be
 * registered on a single day. Handled globally by {@link DailyOwnerLimitExceededExceptionHandler},
 * which responds 429 (Too Many Requests).
 */
public class DailyOwnerLimitExceededException extends Exception {

    private final LocalDate date;

    public DailyOwnerLimitExceededException(LocalDate date) {
        super("Daily owner registration limit reached for " + date);
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }
}

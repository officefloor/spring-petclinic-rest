package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when a create-owner request exceeds the daily sign-up limit — 100 or more owners have
 * already been created today (by registration date). Handled globally by
 * {@link OwnerDailyLimitExceptionHandler}, which responds 429 Too Many Requests.
 */
public class OwnerDailyLimitException extends Exception {

    public OwnerDailyLimitException(LocalDate date, int count) {
        super("Daily owner creation limit reached (" + count + " owners on " + date + ")");
    }
}

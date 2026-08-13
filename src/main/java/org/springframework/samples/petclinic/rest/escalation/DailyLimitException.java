package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.EnsureDailyCapacity}
 * when a create-owner request arrives after 100 or more owners have already been
 * registered today (compared by {@code registrationDate}). Handled by
 * {@link DailyLimitExceptionHandler}, which responds 429.
 */
public class DailyLimitException extends Exception {

    public DailyLimitException(LocalDate date) {
        super("Daily owner-creation limit reached: " + date);
    }
}

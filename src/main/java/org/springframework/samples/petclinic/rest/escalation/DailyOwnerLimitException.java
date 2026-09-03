package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner once 100 or more owners have already been created
 * today (by registrationDate). Handled by {@link DailyOwnerLimitHandler}, which
 * responds 429.
 */
public class DailyOwnerLimitException extends Exception {

    public DailyOwnerLimitException(java.time.LocalDate day) {
        super("Daily owner creation limit reached for " + day);
    }
}

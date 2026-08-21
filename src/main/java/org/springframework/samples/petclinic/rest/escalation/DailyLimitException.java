package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when 100 or more owners have already been registered today (by registrationDate), so a
 * further create is rejected. Handled by {@link DailyLimitExceptionHandler}, which responds 429.
 */
public class DailyLimitException extends Exception {

    public DailyLimitException(int limit) {
        super("The daily limit of owner registrations has been reached: " + limit);
    }
}

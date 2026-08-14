package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.CheckDailyLimit}
 * when 100 or more owners have already been registered today (by registrationDate). Handled
 * globally by {@link DailyLimitExceededExceptionHandler}, which responds 429.
 */
public class DailyLimitExceededException extends Exception {

    public DailyLimitExceededException(int count) {
        super("Daily owner creation limit reached: " + count + " owners already registered today");
    }
}

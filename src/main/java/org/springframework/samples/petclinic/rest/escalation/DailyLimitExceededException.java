package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when the number of owners already registered today has reached the daily limit.
 * Handled by {@link DailyLimitExceededExceptionHandler}, which responds 429.
 */
public class DailyLimitExceededException extends Exception {

    public DailyLimitExceededException(long registeredToday) {
        super("Daily owner registration limit reached: " + registeredToday);
    }
}

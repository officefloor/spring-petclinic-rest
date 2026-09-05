package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.RequireDailyCapacity}
 * when the maximum number of owners has already been registered today.
 * Handled by {@link DailyLimitExceptionHandler}, which responds 429.
 */
public class DailyLimitException extends Exception {

    public DailyLimitException(long count) {
        super("Daily owner limit reached: " + count);
    }
}

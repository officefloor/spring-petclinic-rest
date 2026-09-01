package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner would exceed the number of owners allowed to be created in a single
 * day. Handled by {@link DailyLimitExceededExceptionHandler} as 429.
 */
public class DailyLimitExceededException extends Exception {

    public DailyLimitExceededException(int count) {
        super("Daily owner creation limit reached: " + count + " owners already created today");
    }
}

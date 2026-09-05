package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link RejectOverDailyLimit} when 100 or more owners have already been created today
 * (by {@code registrationDate}). Handled globally by {@code DailyLimitExceededExceptionHandler},
 * which responds 429.
 */
public class DailyLimitExceededException extends Exception {

    public DailyLimitExceededException(int limit) {
        super("Daily owner creation limit of " + limit + " has been reached; try again tomorrow.");
    }
}

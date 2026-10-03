package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown on create when 100 or more owners have already been created today (by
 * registrationDate), i.e. the daily sign-up limit is reached. Handled globally by
 * {@link DailyLimitExceededExceptionHandler}, which responds 429 Too Many Requests.
 */
public class DailyLimitExceededException extends Exception {

    public DailyLimitExceededException(long count) {
        super("Daily owner creation limit reached (" + count + " owners already created today)");
    }
}

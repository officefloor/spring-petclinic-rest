package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create pipeline when 100 or more owners have already been created today
 * (by registration date). Handled globally by
 * {@link DailyOwnerLimitExceededExceptionHandler}, which responds 429.
 */
public class DailyOwnerLimitExceededException extends Exception {

    public DailyOwnerLimitExceededException(String message) {
        super(message);
    }
}

package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request arrives after the maximum number of owners have already
 * been registered on the current day. Handled by
 * {@link DailyLimitExceededExceptionHandler}, which responds 429 Too Many Requests.
 */
public class DailyLimitExceededException extends Exception {

    public DailyLimitExceededException(int limit) {
        super(limit + " or more owners have already been created today");
    }
}

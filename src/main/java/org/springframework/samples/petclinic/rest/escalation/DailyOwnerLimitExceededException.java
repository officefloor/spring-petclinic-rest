package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an <code>Owner</code> would exceed the maximum number of owners
 * that may be registered on a single day (by registration date). Handled globally by
 * {@link DailyOwnerLimitExceededExceptionHandler}, which responds 400 Bad Request.
 */
public class DailyOwnerLimitExceededException extends Exception {

    public DailyOwnerLimitExceededException(String message) {
        super(message);
    }
}

package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when attempting to create an <code>Owner</code> once the maximum number of owners
 * allowed to be registered on a single day has already been reached. Handled globally by
 * {@link DailyOwnerLimitExceptionHandler}, which responds 400 Bad Request.
 */
public class DailyOwnerLimitException extends Exception {

    public DailyOwnerLimitException(String message) {
        super(message);
    }
}

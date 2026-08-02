package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an <code>Owner</code> would exceed the maximum number of owners
 * permitted in a single city. Handled globally by
 * {@link CityOwnerLimitExceededExceptionHandler}, which responds 400 Bad Request.
 */
public class CityOwnerLimitExceededException extends Exception {

    public CityOwnerLimitExceededException(String message) {
        super(message);
    }
}

package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose city already contains the maximum number of
 * owners. Handled globally by {@link CityOwnerLimitExceededExceptionHandler}, which
 * responds 400 Bad Request.
 */
public class CityOwnerLimitExceededException extends Exception {

    public CityOwnerLimitExceededException(String message) {
        super(message);
    }
}

package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner would exceed the maximum number of owners allowed in a
 * single city. Handled globally by {@link CityOwnerLimitExceptionHandler}, which
 * responds 400 Bad Request.
 */
public class CityOwnerLimitException extends Exception {

    public CityOwnerLimitException(String message) {
        super(message);
    }
}

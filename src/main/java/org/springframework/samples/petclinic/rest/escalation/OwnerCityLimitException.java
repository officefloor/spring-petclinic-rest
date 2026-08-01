package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when attempting to create an <code>Owner</code> in a city that already contains
 * the maximum number of owners allowed. Handled globally by
 * {@link OwnerCityLimitExceptionHandler}, which responds 400 Bad Request.
 */
public class OwnerCityLimitException extends Exception {

    public OwnerCityLimitException(String message) {
        super(message);
    }
}

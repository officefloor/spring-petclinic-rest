package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when attempting to create an owner in a city that has already reached its
 * cap of registrations (by city).
 * Handled globally by {@link CityOwnerLimitExceededExceptionHandler}, which responds 400.
 */
public class CityOwnerLimitExceededException extends Exception {

    public CityOwnerLimitExceededException(String message) {
        super(message);
    }
}

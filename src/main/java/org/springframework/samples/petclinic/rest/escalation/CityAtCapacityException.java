package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code EnsureOwnerCityWithinCapacity} when a create request's city already
 * holds the maximum number of owners. Handled by
 * {@link CityAtCapacityExceptionHandler}, which responds 409.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String message) {
        super(message);
    }
}

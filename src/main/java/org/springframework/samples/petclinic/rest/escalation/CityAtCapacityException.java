package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code EnsureCityHasCapacity} when a new owner's city already contains 50 or more
 * owners. Handled globally by {@link CityAtCapacityExceptionHandler}, which responds 409.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String message) {
        super(message);
    }
}

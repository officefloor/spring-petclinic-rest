package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request names a city that already contains 50 or more
 * owners. Handled by {@link CityAtCapacityHandler}, which responds 409.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String message) {
        super(message);
    }
}

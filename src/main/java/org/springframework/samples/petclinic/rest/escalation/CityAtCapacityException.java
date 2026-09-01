package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request targets a city that already contains 50 or more owners.
 * Handled by {@link CityAtCapacityExceptionHandler}, which responds 409 Conflict.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String city) {
        super("City is at capacity: " + city);
    }
}

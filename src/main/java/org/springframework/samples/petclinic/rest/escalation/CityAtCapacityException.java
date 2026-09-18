package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request names a city that already holds the maximum number of owners
 * (50 or more), so no further owner may be registered there. Handled by
 * {@link CityAtCapacityExceptionHandler}, which responds 409 Conflict.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String city, long count) {
        super("City is at capacity (" + count + " owners): " + city);
    }
}

package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request targets a city that already holds the maximum number
 * of owners. Handled by {@link CityAtCapacityExceptionHandler} as a 409.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String city, int capacity) {
        super("City already at capacity (" + capacity + " owners): " + city);
    }
}

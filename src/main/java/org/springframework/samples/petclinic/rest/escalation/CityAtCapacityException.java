package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request targets a city that already contains the maximum number
 * of owners. Handled by {@link CityAtCapacityExceptionHandler}, which responds 409
 * Conflict.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String city, int capacity) {
        super("The city '" + city + "' already contains " + capacity + " or more owners");
    }
}

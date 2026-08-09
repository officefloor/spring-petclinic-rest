package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner in a city that already holds the maximum number of owners (50).
 * Handled globally by {@link CityAtCapacityExceptionHandler}, which responds 409 (Conflict).
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String city, int limit) {
        super("City '" + city + "' already contains " + limit
                + " or more owners; no further owners can be created there");
    }
}

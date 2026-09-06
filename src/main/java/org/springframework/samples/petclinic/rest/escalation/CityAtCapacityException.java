package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request targets a city that already holds the maximum number
 * of owners (50, compared case-insensitively). Handled as a 409 Conflict.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String city, int count) {
        super("City '" + city + "' already contains " + count
                + " owners; no further owners can be created there");
    }
}

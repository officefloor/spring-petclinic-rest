package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckCityCapacity} when a create-owner request names a city that already
 * contains the maximum number of owners (50). Compared case-insensitively against every existing
 * owner's city. Handled by {@link CityAtCapacityExceptionHandler}, which responds 409.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String city, int limit) {
        super("City '" + city + "' already contains " + limit
                + " or more owners; no further owners can be added to it.");
    }
}

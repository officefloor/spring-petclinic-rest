package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request targets a city that already contains the maximum
 * number of owners. Handled by {@link CityAtCapacityExceptionHandler}, which responds 409.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String city, int capacity) {
        super("City " + city + " already contains " + capacity + " or more owners");
    }
}

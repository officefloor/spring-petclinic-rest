package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request targets a city that already contains 50 or more owners.
 * Handled by {@link CityAtCapacityExceptionHandler}, which responds 409 Conflict.
 */
public class CityAtCapacityException extends Exception {

    private final String city;

    public CityAtCapacityException(String city) {
        super("The city has reached its owner capacity: " + city);
        this.city = city;
    }

    public String getCity() {
        return this.city;
    }
}

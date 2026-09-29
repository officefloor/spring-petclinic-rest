package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose city already contains 50 or more owners.
 * Handled globally by {@link CityAtCapacityExceptionHandler}, which responds 409.
 */
public class CityAtCapacityException extends Exception {

    private final String city;

    private final int count;

    public CityAtCapacityException(String city, int count) {
        super("City " + city + " already has " + count + " owners and cannot accept more");
        this.city = city;
        this.count = count;
    }

    /** The city that has reached capacity. */
    public String getCity() {
        return this.city;
    }

    /** The number of owners already in the city. */
    public int getCount() {
        return this.count;
    }
}

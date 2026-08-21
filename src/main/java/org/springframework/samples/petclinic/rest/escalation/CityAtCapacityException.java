package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request targets a city that already contains 50 or more owners (city
 * compared case-insensitively). Handled globally by {@link CityAtCapacityExceptionHandler}, which
 * responds 409.
 */
public class CityAtCapacityException extends Exception {

    private final String city;

    private final int count;

    public CityAtCapacityException(String city, int count) {
        super("City at capacity: " + city + " already has " + count + " owners");
        this.city = city;
        this.count = count;
    }

    public String getCity() {
        return this.city;
    }

    public int getCount() {
        return this.count;
    }
}

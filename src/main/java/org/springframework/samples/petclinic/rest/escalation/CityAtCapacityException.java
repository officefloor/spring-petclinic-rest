package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request would add an owner to a city that already contains the maximum
 * number of owners (50). Handled by {@link CityAtCapacityExceptionHandler}, which responds 409
 * (Conflict).
 */
public class CityAtCapacityException extends Exception {

    private final String city;

    private final int count;

    public CityAtCapacityException(String city, int count) {
        super("City '" + city + "' already contains " + count + " owners (limit 50)");
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

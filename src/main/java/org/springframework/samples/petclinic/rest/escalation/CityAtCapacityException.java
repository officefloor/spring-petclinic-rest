package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code EnsureCityCapacity} when a create-owner request names a city that already
 * contains 50 or more owners. Handled by {@link CityAtCapacityExceptionHandler}, which responds 409
 * so a full city cannot register another owner.
 */
public class CityAtCapacityException extends Exception {

    private final String city;

    private final int count;

    public CityAtCapacityException(String city, int count) {
        super("City '" + city + "' already contains " + count + " owners (capacity is 50)");
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

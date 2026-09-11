package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request would add an owner to a city that already contains 50 or
 * more owners. Handled by {@link OwnerCityAtCapacityExceptionHandler}, which responds
 * 409 Conflict.
 */
public class OwnerCityAtCapacityException extends Exception {

    private final String city;

    private final int count;

    public OwnerCityAtCapacityException(String city, int count) {
        super("City is at capacity: " + city + " already has " + count + " owners");
        this.city = city;
        this.count = count;
    }

    public String getCity() {
        return city;
    }

    public int getCount() {
        return count;
    }
}

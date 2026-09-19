package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request names a city that already holds the maximum number of
 * owners. Handled by {@link CityAtCapacityExceptionHandler}, which responds 409 with the
 * offending city and its capacity.
 */
public class CityAtCapacityException extends Exception {

    private final String city;

    private final int capacity;

    public CityAtCapacityException(String city, int capacity) {
        super("City is at capacity (" + capacity + " owners): " + city);
        this.city = city;
        this.capacity = capacity;
    }

    public String getCity() {
        return this.city;
    }

    public int getCapacity() {
        return this.capacity;
    }
}

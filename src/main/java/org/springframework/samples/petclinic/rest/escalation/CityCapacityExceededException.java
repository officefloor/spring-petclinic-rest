package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request targets a city that already contains the maximum
 * allowed number of owners. Handled by {@link CityCapacityExceededExceptionHandler},
 * which responds 409.
 */
public class CityCapacityExceededException extends Exception {

    private final String city;

    private final long capacity;

    public CityCapacityExceededException(String city, long capacity) {
        super("City is at capacity (" + capacity + " owners): " + city);
        this.city = city;
        this.capacity = capacity;
    }

    public String getCity() {
        return this.city;
    }

    public long getCapacity() {
        return this.capacity;
    }
}

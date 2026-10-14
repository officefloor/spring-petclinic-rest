package org.springframework.samples.petclinic.rest.validation;

/**
 * Thrown when a new owner's city already contains the maximum number of owners.
 */
public class CityCapacityExceededException extends RuntimeException {

    private final String city;

    private final int capacity;

    public CityCapacityExceededException(String city, int capacity) {
        super("The owner's city already contains the maximum number of owners");
        this.city = city;
        this.capacity = capacity;
    }

    public String getCity() {
        return city;
    }

    public int getCapacity() {
        return capacity;
    }
}

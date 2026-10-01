package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link CheckOwnerCityCapacity} when a create request's city already contains 50 or more
 * owners. Handled globally by {@code OwnerCityAtCapacityExceptionHandler}, which responds 409
 * Conflict.
 */
public class OwnerCityAtCapacityException extends Exception {

    private final String city;

    public OwnerCityAtCapacityException(String city) {
        super("City is at capacity: " + city);
        this.city = city;
    }

    public String getCity() {
        return city;
    }
}

package org.springframework.samples.petclinic.util;

/**
 * Thrown when an owner cannot be registered because its city has reached its registration
 * capacity. Handled by the REST layer as a 409 Conflict.
 */
public class CityCapacityException extends RuntimeException {

    public CityCapacityException() {
        super("The owner's city has reached its registration capacity");
    }
}

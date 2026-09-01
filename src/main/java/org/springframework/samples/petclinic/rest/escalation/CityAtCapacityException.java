package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose city already contains the maximum number of owners.
 * Handled by {@link CityAtCapacityExceptionHandler} as 409.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String city, int capacity) {
        super("City at capacity: " + city + " already has " + capacity + " owners");
    }
}

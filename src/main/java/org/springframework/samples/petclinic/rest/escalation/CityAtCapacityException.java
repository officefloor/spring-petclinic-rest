package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request names a city that already contains the maximum number of
 * owners (50). Handled by {@link CityAtCapacityExceptionHandler}, which responds 409.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String city) {
        super("City is at capacity; no more owners may be created in: " + city);
    }
}

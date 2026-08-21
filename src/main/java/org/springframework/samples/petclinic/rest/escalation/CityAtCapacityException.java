package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request names a city that already contains 50 or more owners, so a
 * further owner in that city is rejected. Handled by
 * {@link CityAtCapacityExceptionHandler}, which responds 409.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String city) {
        super("This city has reached its capacity of owners: " + city);
    }
}

package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code RejectCityAtCapacity} when a create request names a city that already contains
 * the maximum number of owners. Handled by {@link CityAtCapacityExceptionHandler}, which responds
 * 409.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String city) {
        super("City at capacity: " + city);
    }
}

package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.CheckCityCapacity}
 * when a create-owner request names a city that already contains 50 or more owners. Handled
 * globally by {@link CityAtCapacityExceptionHandler}, which responds 409.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String city) {
        super("City at capacity: " + city);
    }
}

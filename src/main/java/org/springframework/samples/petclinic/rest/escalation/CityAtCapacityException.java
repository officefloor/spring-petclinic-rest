package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create pipeline when the city of the owner being created already
 * contains 50 or more existing owners. Handled globally by
 * {@link CityAtCapacityExceptionHandler}, which responds 409.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String message) {
        super(message);
    }
}

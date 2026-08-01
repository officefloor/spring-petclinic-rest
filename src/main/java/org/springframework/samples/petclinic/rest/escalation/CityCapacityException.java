package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner would exceed the maximum number of owners that may be
 * registered in a single city. Handled globally by
 * {@link CityCapacityExceptionHandler}, which responds 400 Bad Request.
 */
public class CityCapacityException extends Exception {

    public CityCapacityException(String message) {
        super(message);
    }
}

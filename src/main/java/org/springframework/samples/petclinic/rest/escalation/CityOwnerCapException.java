package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckCityOwnerCap} when a create request would exceed the maximum number of
 * owners allowed to live in a single city. Handled globally by
 * {@link CityOwnerCapExceptionHandler}, which responds 400 Bad Request.
 */
public class CityOwnerCapException extends Exception {

    public CityOwnerCapException(String message) {
        super(message);
    }
}

package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose city already contains the maximum number of
 * owners (50). Cities are compared case-insensitively with collapsed whitespace.
 * Handled globally by {@link OwnerCityAtCapacityExceptionHandler}, which responds
 * 409 Conflict.
 */
public class OwnerCityAtCapacityException extends Exception {

    public OwnerCityAtCapacityException(String message) {
        super(message);
    }
}

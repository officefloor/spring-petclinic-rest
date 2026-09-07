package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the owner's city already contains 50 or more owners
 * (compared case-insensitively with trimmed whitespace). Handled globally by
 * {@link CityAtCapacityExceptionHandler}, which responds 409 Conflict.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String message) {
        super(message);
    }
}

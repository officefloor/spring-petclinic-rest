package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose city already contains 50 or more owners (the per-city
 * capacity). Handled globally by {@link OwnerCityCapacityConflictExceptionHandler}, which
 * responds 409.
 */
public class OwnerCityCapacityConflictException extends Exception {

    public OwnerCityCapacityConflictException(String message) {
        super(message);
    }
}

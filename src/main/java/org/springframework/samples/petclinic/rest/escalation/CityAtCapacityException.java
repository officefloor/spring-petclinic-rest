package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the owner-create pipeline when the owner's city already contains 50 or more
 * owners, so the city is at capacity and cannot accept another owner.
 *
 * <p>Handled by {@link CityAtCapacityExceptionHandler}, which responds 409.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String message) {
        super(message);
    }
}

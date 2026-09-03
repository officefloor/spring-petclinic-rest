package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code BuildOwner} when a create request's address is blank once whitespace
 * is trimmed and collapsed. Handled by {@link AddressRequiredExceptionHandler}, which
 * responds 400.
 */
public class AddressRequiredException extends Exception {

    public AddressRequiredException(String message) {
        super(message);
    }
}

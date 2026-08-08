package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner's address is blank once normalized (trimmed and whitespace-collapsed).
 * Handled globally by {@link InvalidAddressExceptionHandler}, which responds 400.
 */
public class InvalidAddressException extends Exception {

    public InvalidAddressException(String message) {
        super(message);
    }
}

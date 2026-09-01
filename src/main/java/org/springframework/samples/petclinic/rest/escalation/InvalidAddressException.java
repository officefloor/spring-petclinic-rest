package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code NormalizeOwnerAddress} when an owner supplies neither a structured
 * {@code addressLine1} nor a flat {@code address}. Handled globally by
 * {@link InvalidAddressExceptionHandler}, which responds 400.
 */
public class InvalidAddressException extends Exception {

    public InvalidAddressException(String message) {
        super(message);
    }
}

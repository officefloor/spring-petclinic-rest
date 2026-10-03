package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown on create when the submitted address is blank after normalization (trimming and
 * collapsing whitespace leaves nothing). Handled globally by
 * {@link InvalidAddressExceptionHandler}, which responds 400.
 */
public class InvalidAddressException extends Exception {

    public InvalidAddressException(String address) {
        super("Address must not be blank after normalization (rejected value: " + address + ")");
    }
}

package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the owner pipelines when the supplied address is blank after
 * normalization (trim, collapse whitespace, upper-case, expand abbreviations).
 * Handled globally by {@link InvalidAddressExceptionHandler}, which responds 400.
 */
public class InvalidAddressException extends Exception {

    public InvalidAddressException(String message) {
        super(message);
    }
}

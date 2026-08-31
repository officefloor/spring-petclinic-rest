package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code NormalizeOwnerTelephone} when a telephone does not reduce to exactly
 * ten digits. Handled globally by {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String message) {
        super(message);
    }
}

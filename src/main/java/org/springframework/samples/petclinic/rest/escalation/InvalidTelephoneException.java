package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code NormalizeOwnerTelephone} when a telephone number cannot be formed into a
 * valid E.164 number (8 to 15 digits after the '+'). Handled globally by
 * {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String message) {
        super(message);
    }
}

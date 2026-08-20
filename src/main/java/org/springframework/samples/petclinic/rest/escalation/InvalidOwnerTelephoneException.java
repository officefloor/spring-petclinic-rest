package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code NormalizeOwnerTelephone} when a create-owner telephone cannot be converted
 * into a valid E.164 number (8 to 15 digits after the '+'). Handled by
 * {@link InvalidOwnerTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidOwnerTelephoneException extends Exception {

    public InvalidOwnerTelephoneException(String telephone) {
        super("Telephone cannot be converted to a valid E.164 number: " + telephone);
    }
}

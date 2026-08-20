package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code NormalizeOwnerTelephone} when a create-owner telephone is not exactly
 * ten digits once every non-digit character has been stripped. Handled by
 * {@link InvalidOwnerTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidOwnerTelephoneException extends Exception {

    public InvalidOwnerTelephoneException(String telephone) {
        super("Telephone must be exactly 10 digits after removing non-digit characters: " + telephone);
    }
}

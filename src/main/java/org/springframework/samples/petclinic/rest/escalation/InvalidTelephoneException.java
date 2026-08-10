package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code ValidateOwnerFields} when a create-owner request's telephone, after every
 * non-digit character is stripped, is not exactly 10 digits. Handled by
 * {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String telephone) {
        super("Telephone must be exactly 10 digits after removing non-digit characters: " + telephone);
    }
}

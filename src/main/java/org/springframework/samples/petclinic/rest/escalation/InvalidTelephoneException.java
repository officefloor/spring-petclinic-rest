package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose telephone, once every non-digit character is stripped,
 * is not exactly 10 digits. Handled by {@link InvalidTelephoneExceptionHandler}, which responds
 * 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String telephone) {
        super("Telephone must contain exactly 10 digits after removing non-digit characters: "
                + telephone);
    }
}

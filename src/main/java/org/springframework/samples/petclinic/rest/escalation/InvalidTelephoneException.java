package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries a telephone that is not exactly ten digits
 * once every non-digit character has been stripped. Handled by
 * {@link InvalidTelephoneExceptionHandler} as a 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException() {
        super("Telephone must contain exactly 10 digits");
    }
}

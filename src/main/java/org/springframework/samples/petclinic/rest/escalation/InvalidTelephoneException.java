package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a telephone that cannot be canonicalized
 * to E.164 form (a leading {@code '+'} followed by 8 to 15 digits).
 * Handled by {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String telephone) {
        super("Telephone cannot be formatted as E.164: " + telephone);
    }
}

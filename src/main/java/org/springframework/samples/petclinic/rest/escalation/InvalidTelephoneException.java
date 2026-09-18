package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries a telephone that cannot be formed into a valid
 * E.164 number (a leading {@code '+'} followed by 8 to 15 digits). Handled by
 * {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String telephone) {
        super("Telephone must form a valid E.164 number, but was: " + telephone);
    }
}

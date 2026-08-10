package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's telephone cannot be normalized to a valid E.164 number
 * (a '+' followed by 8 to 15 digits). Handled by {@link InvalidTelephoneExceptionHandler}, which
 * responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String telephone) {
        super("Telephone cannot be normalized to a valid E.164 number: " + telephone);
    }
}

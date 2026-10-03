package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown on create when the submitted telephone does not contain exactly 10 digits
 * after every non-digit character has been stripped. Handled globally by
 * {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String telephone) {
        super("Telephone must contain exactly 10 digits after removing non-digit characters (rejected value: "
                + telephone + ")");
    }
}

package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose telephone cannot be converted to a valid E.164 number
 * (a '+' followed by 8 to 15 digits). Handled by {@link InvalidTelephoneExceptionHandler}, which
 * responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String telephone) {
        super("Telephone cannot be converted to a valid E.164 number (+ followed by 8 to 15 "
                + "digits): " + telephone);
    }
}

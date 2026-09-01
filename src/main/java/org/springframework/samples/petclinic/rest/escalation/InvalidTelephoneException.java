package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by BuildOwner when a telephone number does not contain exactly 10 digits after every
 * non-digit character is stripped. Handled by {@link InvalidTelephoneExceptionHandler} as 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String telephone) {
        super("Telephone must contain exactly 10 digits, but was: " + telephone);
    }
}

package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries a telephone that cannot form a valid E.164
 * number (8 to 15 digits after the '+' once separators are stripped and a country code is
 * applied). Handled by {@link InvalidTelephoneExceptionHandler} as a 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException() {
        super("Telephone must form a valid E.164 number (8 to 15 digits after the '+')");
    }
}

package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown on create when the submitted telephone cannot be normalized to a valid E.164
 * number (8 to 15 digits after the '+', with the national-number length matching a
 * recognized country code: '+61' => 9, '+1' => 10). Handled globally by
 * {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String telephone) {
        super("Telephone must form a valid E.164 number with 8 to 15 digits after the '+' "
                + "and a national-number length matching its country code ('+61' => 9, "
                + "'+1' => 10) (rejected value: " + telephone + ")");
    }
}

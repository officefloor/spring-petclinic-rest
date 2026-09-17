package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request's telephone is a well-formed E.164 number but its national-number
 * length is wrong for its country code (e.g. '+61' requires 9 national digits, '+1' requires 10).
 * Carries the offending value so {@link InvalidTelephoneLengthExceptionHandler} can report it. Handled
 * as a 400, distinct from {@link InvalidTelephoneException}'s 400 for a number that is not valid E.164
 * at all.
 */
public class InvalidTelephoneLengthException extends Exception {

    private final String telephone;

    public InvalidTelephoneLengthException(String telephone) {
        super("Telephone has the wrong national-number length for its country code: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return telephone;
    }
}

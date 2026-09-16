package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a telephone whose national-number length is
 * wrong for its E.164 country calling code (e.g. {@code +61} requires 9 national digits,
 * {@code +1} requires 10). Handled by {@link InvalidTelephoneLengthExceptionHandler},
 * which responds 400.
 */
public class InvalidTelephoneLengthException extends Exception {

    public InvalidTelephoneLengthException(String telephone) {
        super("Telephone national-number length is invalid for its country code: " + telephone);
    }
}

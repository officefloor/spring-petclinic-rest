package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a telephone number cannot form a valid E.164 value: it falls outside the 8-to-15
 * digit bound, or it carries an explicit country code whose national-number length is wrong for
 * that country ({@code '+61'} requires 9 national digits, {@code '+1'} requires 10). Handled
 * globally by {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String message) {
        super(message);
    }
}

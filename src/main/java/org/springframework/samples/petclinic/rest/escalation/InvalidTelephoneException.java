package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create pipeline when the supplied telephone cannot be formatted as a valid
 * E.164 number (not 8 to 15 digits after the country code, or the wrong national-number
 * length for a recognised country code: '+61' requires 9 national digits, '+1' requires 10).
 * Handled globally by {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String message) {
        super(message);
    }
}

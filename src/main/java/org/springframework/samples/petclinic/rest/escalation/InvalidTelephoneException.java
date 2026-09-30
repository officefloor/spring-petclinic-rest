package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code NormalizeOwnerTelephone} when a telephone number cannot be formed into a
 * valid E.164 number — either it has the wrong overall digit count (8 to 15 after the '+') or its
 * national number does not match the length its country code requires ('+61' => 9, '+1' => 10).
 * Handled globally by {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String message) {
        super(message);
    }
}

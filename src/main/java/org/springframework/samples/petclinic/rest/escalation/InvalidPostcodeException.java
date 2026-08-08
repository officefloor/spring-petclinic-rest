package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner supplies a 4-digit postcode that is out of range for the
 * region derived from the owner's city (NSW 2000-2099, VIC 3000-3099,
 * QLD 4000-4099). Handled globally by {@link InvalidPostcodeExceptionHandler},
 * which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String message) {
        super(message);
    }
}

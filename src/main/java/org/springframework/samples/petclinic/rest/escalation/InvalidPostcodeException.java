package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the owner pipelines when a supplied postcode is malformed (not 4 digits) or out of
 * range for the owner's city region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). Handled globally
 * by {@link InvalidPostcodeExceptionHandler}, which responds 400. Postcode is optional, so an absent
 * postcode never triggers this.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String message) {
        super(message);
    }
}

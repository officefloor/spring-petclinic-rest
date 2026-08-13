package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.ValidatePostcode}
 * when a create-owner request supplies a postcode that is not four digits, or is out of the
 * valid range for the city's region per the fixed table (NSW 2000-2099, VIC 3000-3099,
 * QLD 4000-4099). Handled by {@link InvalidPostcodeExceptionHandler}, which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String message) {
        super(message);
    }
}

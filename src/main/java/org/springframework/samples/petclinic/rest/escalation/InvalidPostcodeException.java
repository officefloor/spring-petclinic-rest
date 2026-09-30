package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code ValidateOwnerPostcode} when a supplied postcode is out of the valid range for the
 * owner's city region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). Handled globally by
 * {@link InvalidPostcodeExceptionHandler}, which responds 400. Postcode is validated only when
 * present, so an absent postcode never reaches this check.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String message) {
        super(message);
    }
}

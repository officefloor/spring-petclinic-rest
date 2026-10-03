package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the owner pipelines when a supplied postcode is not valid for the
 * owner's city: either not a 4-digit value, or outside the inclusive postcode range
 * of the city's known region (Sydney=NSW 2000-2099, Melbourne=VIC 3000-3099,
 * Brisbane=QLD 4000-4099). A city with no known region accepts any 4-digit postcode,
 * and an absent postcode is never rejected. Handled globally by
 * {@link InvalidPostcodeExceptionHandler}, which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String message) {
        super(message);
    }
}

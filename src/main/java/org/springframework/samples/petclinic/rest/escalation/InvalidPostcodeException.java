package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the owner pipelines when a supplied {@code postcode} is not valid for the owner's
 * city: either it is not 4 digits, or it falls outside the inclusive range of the city's known
 * region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). Postcode is validated only when present.
 *
 * <p>Handled by {@link InvalidPostcodeExceptionHandler}, which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String message) {
        super(message);
    }
}

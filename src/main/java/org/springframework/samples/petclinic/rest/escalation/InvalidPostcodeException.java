package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request carries a postcode that is present but not valid for the
 * owner's city: it is not four digits, or it falls outside the postcode range of the city's
 * region. Handled by {@link InvalidPostcodeExceptionHandler}, which responds 400 with the
 * rejected value.
 */
public class InvalidPostcodeException extends Exception {

    private final String rejectedValue;

    public InvalidPostcodeException(String rejectedValue, String city) {
        super("Postcode '" + rejectedValue + "' is not valid for city: " + city);
        this.rejectedValue = rejectedValue;
    }

    public String getRejectedValue() {
        return this.rejectedValue;
    }
}

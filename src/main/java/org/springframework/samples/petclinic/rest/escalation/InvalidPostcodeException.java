package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a 'postcode' that is not four digits, or that falls
 * outside the range of the owner's city region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city
 * with no known region accepts any 4-digit postcode. Handled by {@link InvalidPostcodeExceptionHandler},
 * which responds 400. Postcode is optional: when absent it is not validated.
 */
public class InvalidPostcodeException extends Exception {

    private final String rejectedValue;

    public InvalidPostcodeException(String rejectedValue, String message) {
        super(message);
        this.rejectedValue = rejectedValue;
    }

    public String getRejectedValue() {
        return this.rejectedValue;
    }
}

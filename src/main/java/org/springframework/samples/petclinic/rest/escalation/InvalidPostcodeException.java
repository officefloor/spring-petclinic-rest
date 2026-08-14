package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code ValidateOwnerPostcode} when a supplied postcode is malformed (not exactly four
 * digits) or is out of the valid range for the owner's city region. Handled globally by
 * {@link InvalidPostcodeExceptionHandler}, which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    private final String postcode;

    private final String city;

    public InvalidPostcodeException(String postcode, String city, String reason) {
        super("Invalid postcode '" + postcode + "' for city '" + city + "': " + reason);
        this.postcode = postcode;
        this.city = city;
    }

    public String getPostcode() {
        return this.postcode;
    }

    public String getCity() {
        return this.city;
    }
}

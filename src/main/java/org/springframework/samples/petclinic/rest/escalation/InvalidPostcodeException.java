package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner's supplied postcode is out of range for the owner's city. Postcodes
 * are validated only when present: for a city with a known region (Sydney=NSW 2000-2099,
 * Melbourne=VIC 3000-3099, Brisbane=QLD 4000-4099) the 4-digit postcode must fall within that
 * region's inclusive range; a city with no known region accepts any 4-digit postcode. Handled
 * globally by {@link InvalidPostcodeExceptionHandler}, which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    private final String city;

    private final String rejectedValue;

    public InvalidPostcodeException(String city, String rejectedValue) {
        super("Postcode " + rejectedValue + " is not valid for city " + city);
        this.city = city;
        this.rejectedValue = rejectedValue;
    }

    /** The city whose region the postcode failed to match. */
    public String getCity() {
        return this.city;
    }

    /** The postcode value that was rejected. */
    public String getRejectedValue() {
        return this.rejectedValue;
    }
}

package org.springframework.samples.petclinic.rest.escalation;

/**
 * Raised when a create-owner request carries a 4-digit {@code postcode} that falls outside
 * the valid range for its city's region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099).
 * Carries the offending postcode and the city so the handler can report what was seen.
 * Malformed postcodes (not four digits) are rejected earlier by bean validation; this
 * exception covers only a well-formed postcode that is out of range for a known region.
 */
public class InvalidPostcodeException extends Exception {

    private final String postcode;

    private final String city;

    public InvalidPostcodeException(String postcode, String city) {
        super("Postcode '" + postcode + "' is not valid for city '" + city + "'");
        this.postcode = postcode;
        this.city = city;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getCity() {
        return city;
    }
}

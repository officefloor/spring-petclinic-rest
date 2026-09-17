package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create/update request supplies a postcode that is not four digits, or that is a
 * valid 4-digit code but falls outside the range its city's region permits. Carries the offending
 * postcode and city so {@link InvalidPostcodeExceptionHandler} can report them. Handled as a 400.
 * Only raised when a postcode is present; an owner with no postcode is unaffected.
 */
public class InvalidPostcodeException extends Exception {

    private final String postcode;

    private final String city;

    public InvalidPostcodeException(String postcode, String city) {
        super("Postcode '" + postcode + "' is not valid for city: " + city);
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

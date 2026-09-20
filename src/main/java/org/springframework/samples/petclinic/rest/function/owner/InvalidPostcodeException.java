package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link ValidateOwnerPostcode} when a supplied postcode is not four digits or falls
 * outside the pinned range for its city's region (see {@link Postcodes}). Carries the rejected
 * value and city for the handler's message. Checked so it appears in the function's
 * {@code throws} clause and routes to an escalation.
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

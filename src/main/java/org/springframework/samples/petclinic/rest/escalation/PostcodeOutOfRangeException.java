package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code RequirePostcodeForCity} when a supplied 4-digit postcode is outside the inclusive
 * range fixed for the owner's city region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with
 * no known region accepts any 4-digit postcode and never triggers this. Carries the offending values so
 * {@link PostcodeOutOfRangeExceptionHandler} can report a 400.
 */
public class PostcodeOutOfRangeException extends Exception {

    private final String postcode;

    private final String city;

    public PostcodeOutOfRangeException(String postcode, String city) {
        super("Postcode '" + postcode + "' is not valid for city '" + city + "'");
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

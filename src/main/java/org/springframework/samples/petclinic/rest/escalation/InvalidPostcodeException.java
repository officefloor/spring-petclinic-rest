package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request supplies a postcode that is out of the valid range for
 * the city's region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known
 * region accepts any 4-digit postcode, so this is never thrown for such cities.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String postcode, String city, String region) {
        super("Postcode " + postcode + " is not valid for city '" + city + "' in region " + region);
    }
}

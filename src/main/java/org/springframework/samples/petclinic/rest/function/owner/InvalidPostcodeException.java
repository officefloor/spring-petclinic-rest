package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link ValidatePostcode} when a supplied postcode is malformed (not exactly four digits)
 * or, for a city whose region is known, falls outside that region's postcode range. Handled globally by
 * {@code InvalidPostcodeExceptionHandler}, which responds 400. A postcode is validated only when
 * present, so an owner created without a postcode never triggers this.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String message) {
        super(message);
    }

    static InvalidPostcodeException malformed(String postcode) {
        return new InvalidPostcodeException(
                "Postcode '" + postcode + "' is not a valid 4-digit postcode.");
    }

    static InvalidPostcodeException outOfRange(String postcode, String city, String region, int[] range) {
        return new InvalidPostcodeException("Postcode '" + postcode + "' is not valid for city '" + city
                + "' (region " + region + " accepts " + range[0] + "-" + range[1] + ").");
    }
}

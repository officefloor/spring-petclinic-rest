package org.springframework.samples.petclinic.util;

/**
 * Thrown when a supplied postcode is not valid for the owner's city, i.e. it falls outside
 * the inclusive range reserved for the city's region. Handled by the REST layer as a 400
 * Bad Request.
 */
public class InvalidPostcodeException extends RuntimeException {

    public InvalidPostcodeException(String postcode, String city) {
        super("Postcode '" + postcode + "' is not valid for city '" + city + "'");
    }
}

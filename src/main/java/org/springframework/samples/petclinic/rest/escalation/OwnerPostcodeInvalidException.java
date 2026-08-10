package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request supplies a postcode that is not four digits, or that is out of range
 * for the city's known region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). Handled globally by
 * {@link OwnerPostcodeInvalidExceptionHandler}, which responds 400.
 */
public class OwnerPostcodeInvalidException extends Exception {

    public OwnerPostcodeInvalidException(String postcode) {
        super("Postcode is not valid for the owner's city: " + postcode);
    }
}

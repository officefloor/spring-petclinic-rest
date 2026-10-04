package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link ValidateOwnerPostcode} when an owner create request carries a {@code postcode}
 * that is present but invalid: either not four digits, or outside the inclusive range for the
 * city's known region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known region
 * accepts any 4-digit postcode. Checked so it appears in the {@code throws} clause and is routed
 * to its escalation handler, which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String postcode) {
        super("Postcode is not valid for the owner's city: " + postcode);
    }
}

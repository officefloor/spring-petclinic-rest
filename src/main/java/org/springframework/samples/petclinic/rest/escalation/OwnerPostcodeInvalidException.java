package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.ValidateOwner} when an
 * owner postcode is present on the request but is not a valid 4-digit value, or is out of range for
 * the owner's city region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known region
 * accepts any 4-digit value. Handled by {@link OwnerPostcodeInvalidExceptionHandler}, which responds
 * 400.
 */
public class OwnerPostcodeInvalidException extends Exception {

    private final String submitted;

    public OwnerPostcodeInvalidException(String submitted) {
        super("Postcode is not valid for the owner's city: " + submitted);
        this.submitted = submitted;
    }

    public String getSubmitted() {
        return this.submitted;
    }
}

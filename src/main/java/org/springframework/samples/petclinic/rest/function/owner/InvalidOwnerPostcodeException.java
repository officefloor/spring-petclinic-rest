package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link ValidateOwnerFields} and {@link ValidateOwner} when an owner request supplies
 * a {@code postcode} that is present but not a valid 4-digit postcode, or is out of range for the
 * city's region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known region accepts
 * any 4-digit postcode. Handled by {@code InvalidOwnerPostcodeExceptionHandler}, which responds 400.
 */
public class InvalidOwnerPostcodeException extends Exception {

    public InvalidOwnerPostcodeException(String message) {
        super(message);
    }
}

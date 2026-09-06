package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.BuildOwner}
 * when a supplied postcode is out of range for the owner's city region (NSW 2000-2099,
 * VIC 3000-3099, QLD 4000-4099). Handled by {@link InvalidPostcodeExceptionHandler},
 * which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String message) {
        super(message);
    }
}

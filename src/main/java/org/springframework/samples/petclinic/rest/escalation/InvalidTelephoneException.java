package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.BuildOwner}
 * when the supplied telephone cannot be normalized to a valid E.164 number (8 to 15
 * digits after the {@code '+'}). Handled by {@link InvalidTelephoneExceptionHandler},
 * which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String message) {
        super(message);
    }
}

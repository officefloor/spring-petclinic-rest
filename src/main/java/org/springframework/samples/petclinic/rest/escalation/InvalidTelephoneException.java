package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.ValidateOwnerFields}
 * when a create-owner request's telephone cannot be normalized to a valid E.164 number.
 * Handled by {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String message) {
        super(message);
    }
}

package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code ValidateOwnerRegistrationDate} when a supplied registration date is later than
 * the server's current date. Registration cannot be dated in the future. Handled globally by
 * {@link FutureRegistrationDateExceptionHandler}, which responds 400. The registration date is
 * optional on create, so an absent date never reaches this check.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException(String message) {
        super(message);
    }
}

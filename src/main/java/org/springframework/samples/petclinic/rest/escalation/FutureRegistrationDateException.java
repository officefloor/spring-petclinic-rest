package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the supplied {@code registrationDate} is later than the
 * server's current date. A registration cannot be dated in the future. Handled globally by
 * {@link FutureRegistrationDateExceptionHandler}, which responds 400. Registration date is optional,
 * so an absent date never triggers this.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException(String message) {
        super(message);
    }
}

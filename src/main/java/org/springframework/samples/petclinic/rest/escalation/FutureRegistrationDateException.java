package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request supplies a {@code registrationDate} later than the
 * server's current date. A registration date may not lie in the future. Handled
 * globally by {@link FutureRegistrationDateExceptionHandler}, which responds 400.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException(String message) {
        super(message);
    }
}

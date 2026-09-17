package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a registrationDate later than the server's
 * current date. Handled by {@link FutureRegistrationDateExceptionHandler} as a 400.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException() {
        super("Registration date must not be later than the current date");
    }
}

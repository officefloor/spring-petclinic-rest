package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request supplies a registrationDate that is later than the
 * server's current date. A registration cannot be dated in the future, so such a
 * request is rejected with a 400. A registrationDate that is absent (defaulted to the
 * server date) or on/before the server date is accepted and never triggers this.
 */
public class RegistrationDateInFutureException extends Exception {

    public RegistrationDateInFutureException(java.time.LocalDate supplied, java.time.LocalDate serverDate) {
        super("Registration date " + supplied + " is later than the server date " + serverDate);
    }
}

package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when a request to create an owner supplies a {@code registrationDate} later than the server's
 * current date: an owner cannot be registered in the future, so the request is answered with a 400 Bad
 * Request. Carries the offending date so {@link FutureRegistrationDateExceptionHandler} can report what
 * was submitted. Checked so it appears in the function's {@code throws} clause and is routed as an
 * escalation.
 */
public class FutureRegistrationDateException extends Exception {

    private final LocalDate registrationDate;

    public FutureRegistrationDateException(LocalDate registrationDate) {
        super("The registration date must not be later than today: '" + registrationDate + "'");
        this.registrationDate = registrationDate;
    }

    public LocalDate getRegistrationDate() {
        return this.registrationDate;
    }
}

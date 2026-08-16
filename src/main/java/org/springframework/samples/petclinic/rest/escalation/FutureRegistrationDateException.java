package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.ResolveRegistrationDate}
 * when the request supplies a {@code registrationDate} later than the server's current date, so a
 * back-dated registration is allowed but a future-dated one is not. Handled globally by
 * {@link FutureRegistrationDateExceptionHandler}, which responds 400.
 */
public class FutureRegistrationDateException extends Exception {

    private final LocalDate registrationDate;

    public FutureRegistrationDateException(LocalDate registrationDate) {
        super("Registration date must not be later than the server date: " + registrationDate);
        this.registrationDate = registrationDate;
    }

    public LocalDate getRegistrationDate() {
        return this.registrationDate;
    }
}

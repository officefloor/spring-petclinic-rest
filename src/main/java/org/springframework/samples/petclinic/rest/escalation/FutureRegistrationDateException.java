package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown by {@code ValidateOwnerRegistrationDate} when a create-owner request supplies a
 * registrationDate that is later than the server's current date. A future registration date is
 * rejected before it is defaulted or adjusted to a business day. Handled by
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

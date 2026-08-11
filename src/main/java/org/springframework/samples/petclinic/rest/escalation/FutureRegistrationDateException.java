package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Raised when a create-owner request supplies a {@code registrationDate} later than the
 * server date. Registration may be back-dated or omitted (defaulting to today), but it may
 * not be in the future. Carries the supplied date and the server date so the handler can
 * report why the request was rejected (400).
 */
public class FutureRegistrationDateException extends Exception {

    private final LocalDate supplied;

    private final LocalDate today;

    public FutureRegistrationDateException(LocalDate supplied, LocalDate today) {
        super("Registration date " + supplied + " is later than the server date " + today);
        this.supplied = supplied;
        this.today = today;
    }

    public LocalDate getSupplied() {
        return supplied;
    }

    public LocalDate getToday() {
        return today;
    }
}

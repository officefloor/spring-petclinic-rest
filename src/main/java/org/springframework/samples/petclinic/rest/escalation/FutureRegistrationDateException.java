package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when an owner's supplied {@code registrationDate} is later than the server's current
 * date. The registration date is only checked when supplied; a request that omits it defaults
 * to the server date and is always accepted. Handled globally by
 * {@link FutureRegistrationDateExceptionHandler}, which responds 400.
 */
public class FutureRegistrationDateException extends Exception {

    private final LocalDate rejectedValue;

    public FutureRegistrationDateException(LocalDate rejectedValue) {
        super("Registration date " + rejectedValue + " is later than the current date");
        this.rejectedValue = rejectedValue;
    }

    /** The registration date value that was rejected. */
    public LocalDate getRejectedValue() {
        return this.rejectedValue;
    }
}

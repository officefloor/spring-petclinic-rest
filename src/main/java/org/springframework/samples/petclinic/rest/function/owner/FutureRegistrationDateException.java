package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * Thrown by {@link RejectFutureRegistrationDate} when a create-owner request supplies a
 * {@code registrationDate} later than the current server date. Handled globally by
 * {@code FutureRegistrationDateExceptionHandler}, which responds 400. The date is optional, so a
 * request that omits it never triggers this.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException(LocalDate supplied, LocalDate today) {
        super("Registration date '" + supplied + "' is in the future; it must not be later than "
                + today + ".");
    }
}

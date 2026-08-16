package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown by the create-owner pipeline when a supplied {@code registrationDate} is later than the
 * server's current date — a registration cannot be dated in the future.
 *
 * <p>Carries the offending date so {@link OwnerRegistrationDateInvalidExceptionHandler} can respond
 * 400.
 */
public class OwnerRegistrationDateInvalidException extends Exception {

    private final LocalDate registrationDate;

    public OwnerRegistrationDateInvalidException(LocalDate registrationDate) {
        super("Registration date is in the future: " + registrationDate);
        this.registrationDate = registrationDate;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }
}

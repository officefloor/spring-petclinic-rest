package org.springframework.samples.petclinic.service;

import java.time.LocalDate;

/**
 * Rejects a supplied registration date that lies in the future (later than the
 * server's current date). A {@code null} date is allowed and defaulted downstream.
 */
public final class RegistrationDateValidator {

    private RegistrationDateValidator() {
    }

    public static LocalDate validate(LocalDate registrationDate) {
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("registrationDate cannot be later than the server date: " + registrationDate);
        }
        return registrationDate;
    }
}

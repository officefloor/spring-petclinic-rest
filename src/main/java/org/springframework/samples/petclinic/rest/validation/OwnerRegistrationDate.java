package org.springframework.samples.petclinic.rest.validation;

import java.time.LocalDate;

/**
 * Validates optional owner registration dates supplied by clients.
 */
public final class OwnerRegistrationDate {

    private OwnerRegistrationDate() {
    }

    /**
     * Checks a supplied registration date. An absent date is accepted; a present one must not be later
     * than the server's current date. The check applies to the date as supplied, before any weekend
     * roll-forward.
     *
     * @param registrationDate the registration date as supplied in the request, may be {@code null}
     * @throws InvalidRegistrationDateException if the date is later than the server's current date
     */
    public static void validate(LocalDate registrationDate) {
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new InvalidRegistrationDateException(registrationDate.toString(),
                "Registration date must not be later than the current date");
        }
    }
}

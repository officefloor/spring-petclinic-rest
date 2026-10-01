package org.springframework.samples.petclinic.util;

import java.time.LocalDate;

/**
 * Validates an owner's supplied {@code registrationDate}.
 *
 * <p>A registration date is validated ONLY when present; a {@code null} registration date is
 * always accepted (it later defaults to the server's current date on persist). When present it
 * must not be later than the server's current date: a date in the future is rejected, since an
 * owner cannot be registered on a day that has not yet occurred.
 */
public final class RegistrationDateValidator {

    private RegistrationDateValidator() {
    }

    /**
     * Validate a supplied registration date, throwing when it is later than the server's current
     * date.
     *
     * @param registrationDate the supplied registration date, or {@code null} when none was given
     * @throws InvalidRegistrationDateException if the registration date is later than the server
     *         date (i.e. lies in the future)
     */
    public static void validate(LocalDate registrationDate) {
        if (registrationDate == null) {
            return;
        }
        LocalDate serverDate = LocalDate.now();
        if (registrationDate.isAfter(serverDate)) {
            throw new InvalidRegistrationDateException(registrationDate, serverDate);
        }
    }
}

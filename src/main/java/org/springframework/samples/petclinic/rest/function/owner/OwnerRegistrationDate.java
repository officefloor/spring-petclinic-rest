package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Shared registration-date validation for the owner pipelines. A {@code registrationDate} is
 * optional — it defaults to the server date when omitted — but WHEN PRESENT it must not be later
 * than the server's current date: a registration date may not be in the future. A future date is
 * rejected with {@link FutureRegistrationDateException} (handled as 400). The supplied date is
 * checked as-is, before any business-day adjustment.
 */
final class OwnerRegistrationDate {

    private OwnerRegistrationDate() {
    }

    /**
     * Rejects a supplied registration date that is later than the server date; an absent date, or a
     * date on or before today, is accepted.
     */
    static void validate(OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate supplied = request.getRegistrationDate();
        if (supplied != null && supplied.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(supplied);
        }
    }
}

package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code ageBand} from its {@code birthDate} measured against its
 * {@code registrationDate}: {@code MINOR} when under 18, {@code ADULT} when 18-64 and
 * {@code SENIOR} when 65 or older. Returns {@code null} when no birth date is known,
 * so the field is simply absent from the response.
 */
public final class AgeBands {

    private AgeBands() {
    }

    /** The age band for the given owner, or {@code null} when no birth date is set. */
    public static String forOwner(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        // Age is measured against the registration date, defaulting to today when absent.
        LocalDate asOf = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        long years = ChronoUnit.YEARS.between(birthDate, asOf);
        if (years < 18) {
            return "MINOR";
        }
        if (years < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }
}

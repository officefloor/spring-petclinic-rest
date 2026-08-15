package org.springframework.samples.petclinic.rest.function.common;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the owner's {@code ageBand} from the {@code birthDate}, measured against the
 * {@code registrationDate}.
 *
 * <p>The band is {@code MINOR} when the owner is under 18 at registration, {@code ADULT} from 18 to
 * 64 inclusive, and {@code SENIOR} at 65 or older. It is absent (null) when no birth date is on
 * record.
 */
public final class AgeBands {

    private AgeBands() {
    }

    /** The owner's age band, or {@code null} when no birth date is recorded. */
    public static String of(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        LocalDate against = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        int age = Period.between(birthDate, against).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }
}

package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.Period;

/**
 * Derives an owner's age band from their birth date, measured against their registration date.
 *
 * <p>The completed-years age on the registration date decides the band: {@code MINOR} under 18,
 * {@code ADULT} from 18 to 64 inclusive and {@code SENIOR} from 65. Returns {@code null} when
 * no birth date was supplied, so the response omits the field for owners without one.
 *
 * <p>Kept as a standalone class (not a method on {@link OwnerMapper}) so MapStruct does not
 * mistake it for an implicit mapping method.
 */
public final class AgeBand {

    private AgeBand() {
    }

    /**
     * The age band for an owner born on {@code birthDate} as measured on {@code registrationDate},
     * or {@code null} when {@code birthDate} is absent.
     */
    public static String of(LocalDate birthDate, LocalDate registrationDate) {
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int age = Period.between(birthDate, registrationDate).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }
}

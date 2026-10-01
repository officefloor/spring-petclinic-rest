package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Single source of truth for an owner's derived {@code ageBand} — the band the owner falls into
 * based on their {@code birthDate} measured against their {@code registrationDate}:
 * {@code MINOR} when under 18, {@code ADULT} from 18 to 64 inclusive, and {@code SENIOR} at 65 or
 * over.
 *
 * <p>Derived at read time by the owner mapper from the already-stored {@code birthDate} and
 * {@code registrationDate}, so it needs no column of its own and can never drift. Absent
 * ({@code null}) when no birth date was supplied.
 */
public final class OwnerAgeBand {

    private OwnerAgeBand() {
    }

    /** The age band of an already-stored owner, or {@code null} when no birth date is recorded. */
    public static OwnerDto.AgeBandEnum ageBand(Owner owner) {
        return ageBand(owner.getBirthDate(), owner.getRegistrationDate());
    }

    /**
     * The age band for a {@code birthDate} as measured against {@code asOf} (the registration
     * date); {@code null} when either date is absent.
     */
    public static OwnerDto.AgeBandEnum ageBand(LocalDate birthDate, LocalDate asOf) {
        if (birthDate == null || asOf == null) {
            return null;
        }
        int years = Period.between(birthDate, asOf).getYears();
        if (years < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (years < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }
}

package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Derives an owner's {@code ageBand} — the coarse age classification returned on the owner DTO.
 *
 * <p>The band is computed from the owner's {@code birthDate} measured against their
 * {@code registrationDate} (the effective, business-day-adjusted date resolved at creation):
 * {@code MINOR} when under 18, {@code ADULT} from 18 up to and including 64, {@code SENIOR} at 65 or
 * over. When no birth date was supplied the band is absent ({@code null}).
 */
public final class AgeBand {

    private static final int ADULT_AGE = 18;

    private static final int SENIOR_AGE = 65;

    private AgeBand() {
    }

    /** The owner's age band, or {@code null} when no birth date is present. */
    public static OwnerDto.AgeBandEnum of(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        LocalDate reference = owner.getRegistrationDate();
        if (reference == null) {
            reference = LocalDate.now();
        }
        int years = Period.between(birthDate, reference).getYears();
        if (years < ADULT_AGE) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (years < SENIOR_AGE) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }
}

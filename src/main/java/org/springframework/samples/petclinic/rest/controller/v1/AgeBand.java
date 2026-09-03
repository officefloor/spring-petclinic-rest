package org.springframework.samples.petclinic.rest.controller.v1;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Derives an owner's {@code ageBand} from its birth date measured against its
 * registration date: MINOR (under 18), ADULT (18-64) or SENIOR (65+). Returns
 * {@code null} when no birth date is set, so the field is simply absent.
 */
public final class AgeBand {

    private AgeBand() {
    }

    /** The age band of {@code owner}, or {@code null} when it has no birth date. */
    public static OwnerDto.AgeBandEnum of(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        int age = Period.between(birthDate, owner.getRegistrationDate()).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        return age < 65 ? OwnerDto.AgeBandEnum.ADULT : OwnerDto.AgeBandEnum.SENIOR;
    }
}

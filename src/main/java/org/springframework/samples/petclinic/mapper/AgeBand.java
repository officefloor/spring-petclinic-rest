package org.springframework.samples.petclinic.mapper;

import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto.AgeBandEnum;

/**
 * Derives an owner's age band ('ageBand') from its supplied birthDate, measured against the
 * owner's registrationDate: {@link AgeBandEnum#MINOR} when under 18, {@link AgeBandEnum#ADULT}
 * from 18 to 64, and {@link AgeBandEnum#SENIOR} at 65 or over.
 *
 * <p>Kept as a standalone helper (referenced from {@link OwnerMapper}'s {@code ageBand}
 * expression) rather than a mapper {@code default} method to avoid MapStruct picking it up as an
 * automatic conversion.
 */
final class AgeBand {

    private AgeBand() {
    }

    /**
     * The owner's age band, or {@code null} when no birthDate was supplied (so the field is
     * omitted from the response). Age is the completed years between birthDate and
     * registrationDate.
     */
    static AgeBandEnum of(Owner owner) {
        if (owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int age = Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears();
        if (age < 18) {
            return AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return AgeBandEnum.ADULT;
        }
        return AgeBandEnum.SENIOR;
    }
}

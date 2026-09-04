package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * The read-time age band of an owner, derived from the owner's {@code birthDate} measured against
 * the (business-day adjusted) {@code registrationDate}. {@code MINOR} when under 18, {@code ADULT}
 * from 18 to 64 inclusive, and {@code SENIOR} at 65 or over. Absent (null) when either date is
 * unknown, so an owner created without a birthDate reports no age band.
 */
public final class AgeBand {

    private AgeBand() {
    }

    /** The age band for {@code owner}, or {@code null} when it cannot be determined. */
    public static OwnerDto.AgeBandEnum of(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        LocalDate registrationDate = owner.getRegistrationDate();
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int age = Period.between(birthDate, registrationDate).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }
}

package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Derives an owner's age band from their birthDate as of the registrationDate:
 * MINOR (under 18), ADULT (18-64) or SENIOR (65+). Null when no birthDate was
 * supplied, so the field is simply absent from the response.
 */
public final class AgeBand {

    private AgeBand() {
    }

    /** Age band as of the owner's registration date, or null when birthDate is absent. */
    public static OwnerDto.AgeBandEnum of(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        LocalDate asOf = owner.getRegistrationDate();
        if (asOf == null) {
            asOf = LocalDate.now();
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

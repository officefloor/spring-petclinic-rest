package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Derives an owner's age band from its birthDate, measured against the
 * registrationDate: MINOR when under 18, ADULT from 18 to 64, SENIOR at 65 or over.
 * Kept as a plain static helper (not a mapper method) so MapStruct does not treat it
 * as an implicit mapping method.
 */
public final class AgeBands {

    private AgeBands() {
    }

    /**
     * The owner's age band, or null when no birthDate is present. The age is the
     * completed years between birthDate and the registrationDate (defaulting to the
     * current date when the registrationDate is absent).
     */
    public static OwnerDto.AgeBandEnum of(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        LocalDate asOf = owner.getRegistrationDate();
        if (asOf == null) {
            asOf = LocalDate.now();
        }
        int age = Period.between(birthDate, asOf).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }
}

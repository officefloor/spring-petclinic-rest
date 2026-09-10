package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Derives an owner's {@link OwnerDto.AgeBandEnum age band} from the birthDate, computed as
 * the owner's age on the registrationDate: MINOR when under 18, ADULT when 18 to 64, SENIOR
 * when 65 or over. Returns {@code null} when no birthDate was supplied, so the field is
 * absent from the response.
 */
public final class AgeBand {

    private AgeBand() {
    }

    public static OwnerDto.AgeBandEnum of(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        LocalDate reference = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        int age = Period.between(birthDate, reference).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }
}

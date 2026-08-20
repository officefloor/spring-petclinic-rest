package org.springframework.samples.petclinic.rest.function.common;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Derives an owner's {@link OwnerDto.AgeBandEnum age band} from a birth date. The age is the number
 * of full years between the birth date and the owner's registration date; the band is {@code MINOR}
 * under 18, {@code ADULT} from 18 to 64 and {@code SENIOR} at 65 or over.
 */
public final class AgeBands {

    private AgeBands() {
    }

    /**
     * Returns the age band for {@code birthDate} measured against {@code registrationDate}, or
     * {@code null} when no birth date was supplied (so {@code ageBand} is omitted from the response).
     */
    public static OwnerDto.AgeBandEnum of(LocalDate birthDate, LocalDate registrationDate) {
        if (birthDate == null) {
            return null;
        }
        LocalDate reference = registrationDate != null ? registrationDate : LocalDate.now();
        int years = Period.between(birthDate, reference).getYears();
        if (years < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (years < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }
}

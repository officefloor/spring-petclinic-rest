package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Derives the {@code ageBand} field produced by {@link OwnerMapper#toOwnerDto}: the owner's age band.
 *
 * <p>It is computed only when the owner has a {@code birthDate}; the age is the number of whole years
 * between the birth date and the owner's {@code registrationDate}, and the band is {@code MINOR} when
 * under 18, {@code ADULT} from 18 to 64 inclusive, and {@code SENIOR} at 65 or older. When no birth
 * date is present the result is {@code null} so the field is omitted from the response.
 */
public final class AgeBand {

    /** Minimum age (inclusive) for the {@code ADULT} band; below this is {@code MINOR}. */
    private static final int ADULT_FROM = 18;

    /** Minimum age (inclusive) for the {@code SENIOR} band. */
    private static final int SENIOR_FROM = 65;

    private AgeBand() {
    }

    /**
     * Derive the age band for an owner.
     *
     * @param owner the owner; its {@code birthDate} and {@code registrationDate} drive the result
     * @return the band, or {@code null} when the owner has no birth date
     */
    public static OwnerDto.AgeBandEnum of(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        int age = Period.between(birthDate, owner.getRegistrationDate()).getYears();
        if (age < ADULT_FROM) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < SENIOR_FROM) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }
}

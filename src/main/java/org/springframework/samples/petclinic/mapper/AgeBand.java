package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Derives an owner's {@link OwnerDto.AgeBandEnum age band} from its birth date.
 *
 * <p>The band is the number of full years from {@code birthDate} to {@code registrationDate}:
 * {@code MINOR} under 18, {@code ADULT} 18-64, {@code SENIOR} 65 or older. It is {@code null} when
 * either date is absent (no birthDate was supplied on create).
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so MapStruct does not
 * mistake it for a mapping method and apply it to unrelated fields.
 */
public final class AgeBand {

    private AgeBand() {
    }

    /**
     * The age band of {@code owner} computed from its birthDate against its registrationDate, or
     * {@code null} when either date is absent.
     */
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

package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code ageBand} from {@code birthDate} measured against
 * {@code registrationDate}: 'MINOR' when under 18, 'ADULT' from 18 to 64 and 'SENIOR'
 * at 65 or older. Yields {@code null} (so the field is absent) when no birthDate was
 * supplied. Kept out of {@link OwnerMapper} so MapStruct does not mistake the helper
 * for an implicit mapping method.
 */
public final class AgeBand {

    private AgeBand() {
    }

    /** The age band for {@code owner}, or {@code null} when no birthDate is present. */
    public static String of(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        LocalDate asOf = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        int years = Period.between(birthDate, asOf).getYears();
        if (years < 18) {
            return "MINOR";
        }
        if (years < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }
}

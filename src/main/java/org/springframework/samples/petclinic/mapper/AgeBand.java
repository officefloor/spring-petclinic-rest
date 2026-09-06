package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's age band from their {@code birthDate}, measured against the
 * owner's {@code registrationDate}. Kept as a plain static helper - rather than a
 * method on {@link OwnerMapper} - so MapStruct does not mistake it for an implicit
 * property mapping.
 *
 * <p>The band is {@code MINOR} when the owner was under 18 at registration,
 * {@code ADULT} from 18 to 64 inclusive, and {@code SENIOR} at 65 or older. When
 * no {@code birthDate} was supplied the band is {@code null} so the field is
 * omitted from the response.
 */
public final class AgeBand {

    /** Minimum age, in whole years, for the {@code ADULT} band. */
    private static final int ADULT_AGE = 18;

    /** Minimum age, in whole years, for the {@code SENIOR} band. */
    private static final int SENIOR_AGE = 65;

    private AgeBand() {
    }

    /**
     * Computes the owner's age band from {@code birthDate} against
     * {@code registrationDate}.
     *
     * @param owner the owner whose age band to derive
     * @return {@code "MINOR"}, {@code "ADULT"} or {@code "SENIOR"}, or {@code null}
     *     when the owner has no {@code birthDate}
     */
    public static String forOwner(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        LocalDate reference = owner.getRegistrationDate() != null
            ? owner.getRegistrationDate() : LocalDate.now();
        int years = Period.between(birthDate, reference).getYears();
        if (years < ADULT_AGE) {
            return "MINOR";
        }
        if (years < SENIOR_AGE) {
            return "ADULT";
        }
        return "SENIOR";
    }

}

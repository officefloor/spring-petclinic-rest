package org.springframework.samples.petclinic.util;

import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code ageBand} from its {@code birthDate}, measured against its
 * {@code registrationDate}: {@code "MINOR"} when under 18, {@code "ADULT"} from 18 to 64,
 * {@code "SENIOR"} at 65 or older.
 *
 * <p>Returns {@code null} when the owner has no birthDate, so the field is absent from the
 * response for owners created without one.
 */
public final class AgeBands {

    /** Band for owners under 18 at their registration date. */
    public static final String MINOR = "MINOR";

    /** Band for owners aged 18 to 64 inclusive at their registration date. */
    public static final String ADULT = "ADULT";

    /** Band for owners aged 65 or older at their registration date. */
    public static final String SENIOR = "SENIOR";

    private AgeBands() {
    }

    /**
     * @param owner the pet owner.
     * @return {@link #MINOR}, {@link #ADULT} or {@link #SENIOR} derived from the owner's age at
     *         its registrationDate, or {@code null} when the owner has no birthDate.
     */
    public static String bandFor(Owner owner) {
        if (owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int age = Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears();
        if (age < 18) {
            return MINOR;
        }
        if (age < 65) {
            return ADULT;
        }
        return SENIOR;
    }
}

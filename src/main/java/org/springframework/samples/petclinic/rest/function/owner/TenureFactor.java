package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Membership points contributed by the owner's tenure exceeding the qualifying threshold
 * of 365 days. One factor of the overall {@link MembershipPoints} total; owns the tenure
 * test, its threshold and its weight. A newly created owner has zero tenure.
 */
public final class TenureFactor {

    private static final int POINTS = 1;

    private static final long TENURE_DAYS = 365;

    private TenureFactor() {
    }

    public static int points(Owner owner) {
        return qualifies(owner) ? POINTS : 0;
    }

    private static boolean qualifies(Owner owner) {
        LocalDate registered = owner.getRegistrationDate();
        return registered != null && ChronoUnit.DAYS.between(registered, LocalDate.now()) > TENURE_DAYS;
    }
}

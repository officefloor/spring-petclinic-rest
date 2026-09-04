package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Membership points contributed by the owner's tenure exceeding the qualifying threshold
 * of one elapsed {@link FiscalYear}. One factor of the overall {@link MembershipPoints}
 * total; owns the tenure test, its threshold and its weight. A newly created owner has zero tenure.
 */
public final class TenureFactor {

    private static final int POINTS = 3;

    private static final int TENURE_FISCAL_YEARS = 1;

    private TenureFactor() {
    }

    public static int points(Owner owner) {
        return qualifies(owner) ? POINTS : 0;
    }

    private static boolean qualifies(Owner owner) {
        LocalDate registered = owner.getRegistrationDate();
        return registered != null
                && FiscalYear.of(LocalDate.now()) - FiscalYear.of(registered) >= TENURE_FISCAL_YEARS;
    }
}

package org.springframework.samples.petclinic.rest.function.common;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric membership level (1 to 4).
 *
 * <p>The pre-tenure factors are a base of 1, plus one for holding an email address, plus one for a
 * {@code namesakeCount} of 0. Those factors are capped at level 3, so even a fully-endowed new owner
 * (an email, a {@code namesakeCount} of 0 and a multi-member household) never exceeds level 3.
 *
 * <p>Level 4 is reserved for tenure: it requires more than 365 days between the owner's registration
 * date and today. Because a newly created owner has zero tenure, a new owner is always at most
 * level 3.
 */
public final class MembershipLevels {

    /** Level 4 requires strictly more than this many days of tenure. */
    private static final long TENURE_DAYS_FOR_LEVEL_4 = 365;

    private MembershipLevels() {
    }

    public static int of(Owner owner) {
        int preTenure = Math.min(3, 1
                + ((owner.getEmail() != null && !owner.getEmail().isBlank()) ? 1 : 0)
                + ((owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) ? 1 : 0));
        return preTenure + (hasTenureForLevel4(owner.getRegistrationDate()) ? 1 : 0);
    }

    private static boolean hasTenureForLevel4(LocalDate registrationDate) {
        if (registrationDate == null) {
            return false;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now()) > TENURE_DAYS_FOR_LEVEL_4;
    }
}

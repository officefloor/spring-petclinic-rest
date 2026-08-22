package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Derives an owner's numeric membership level. Kept as a plain static helper so the
 * single rule is shared by the response mapper and the create audit line.
 */
public final class MembershipLevels {

    private MembershipLevels() {
    }

    /**
     * The membership level from 1 to 4: start at 1, add 1 when an email is present, add 1
     * when namesakeCount is 0, add 1 when tenure exceeds 365 days, capped at 4. A newly
     * created owner has zero tenure, so it can never reach level 4 on creation.
     */
    public static int of(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        if (tenureDays(owner) > 365) {
            level++;
        }
        return Math.min(level, 4);
    }

    /**
     * Days since the owner registered, or 0 when the registration date is unknown. A new
     * owner registers today, so its tenure is 0.
     */
    private static long tenureDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
    }
}

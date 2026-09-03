package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the owner's numeric membership level, from 1 to 4. Starts at 1; adds 1 when an
 * email is present; adds 1 when the namesake count is 0; capped at 3. Level 4 requires
 * tenure of more than 365 days, so a newly created owner (zero tenure) never exceeds
 * level 3.
 */
public final class MembershipLevel {

    private static final long TENURE_DAYS_FOR_LEVEL_4 = 365;

    private MembershipLevel() {
    }

    public static int of(Owner owner, int namesakeCount) {
        int level = 1;
        if (owner.getEmail() != null) {
            level++;
        }
        if (namesakeCount == 0) {
            level++;
        }
        level = Math.min(level, 3);
        if (level == 3 && hasTenure(owner)) {
            level = 4;
        }
        return level;
    }

    private static boolean hasTenure(Owner owner) {
        LocalDate registration = owner.getRegistrationDate();
        return registration != null
                && ChronoUnit.DAYS.between(registration, LocalDate.now()) > TENURE_DAYS_FOR_LEVEL_4;
    }
}

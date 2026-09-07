package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric membership level (1 to 4): it starts at 1, gains 1
 * when an email is present, gains 1 when {@code namesakeCount} is 0, and those
 * pre-tenure factors are capped at 3. Level 4 is reached only when tenure exceeds
 * 365 days (measured from {@code registrationDate}), so a newly created owner —
 * whose tenure is zero — never exceeds level 3.
 */
public final class MembershipLevel {

    /** Tenure (in days) beyond which an owner qualifies for level 4. */
    private static final long TENURE_LEVEL_4_DAYS = 365;

    private MembershipLevel() {
    }

    /** Membership level (1 to 4) derived from the owner's email, namesake count and tenure. */
    public static int of(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level += 1;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level += 1;
        }
        level = Math.min(level, 3);
        if (tenureDays(owner) > TENURE_LEVEL_4_DAYS) {
            level += 1;
        }
        return level;
    }

    /** Days elapsed since the owner's registrationDate; 0 when the date is absent or in the future. */
    private static long tenureDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
        return Math.max(days, 0);
    }
}

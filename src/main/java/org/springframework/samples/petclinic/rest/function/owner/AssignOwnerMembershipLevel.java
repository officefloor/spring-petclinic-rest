package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs in {@code POST /api/owners} after the owner's namesake count has been recorded and before
 * it is saved, within the create transaction. Records on the built owner its numeric membership
 * level: it starts at 1, gains 1 when an email is present, 1 more when
 * {@link Owner#getNamesakeCount() namesakeCount} is 0, and a final 1 when the owner's tenure
 * exceeds {@link #TENURE_DAYS_FOR_TOP_LEVEL} days, capped at {@link #MAX_LEVEL}. Level 4 therefore
 * requires more than a year of tenure; because a newly registered owner has zero tenure, a new
 * owner never exceeds level 3. Reads the resolved (weekend-adjusted) registration date published
 * by {@link ResolveOwnerRegistrationDate} — the owner is not stamped with it until
 * {@link ApplyOwnerRegistrationDate}, which runs later. Runs after {@link AssignOwnerNamesakeCount}
 * so the count it reads is final.
 */
public class AssignOwnerMembershipLevel {

    /** Tenure, in days, an owner must exceed to earn the tenure step toward the top level. */
    private static final long TENURE_DAYS_FOR_TOP_LEVEL = 365;

    /** The highest membership level; reachable only with tenure beyond a year. */
    private static final int MAX_LEVEL = 4;

    public void service(@Val Owner owner, @Val LocalDate registrationDate) {
        int level = 1;
        if (hasEmail(owner)) {
            level++;
        }
        if (hasUniqueName(owner)) {
            level++;
        }
        if (tenureDays(registrationDate) > TENURE_DAYS_FOR_TOP_LEVEL) {
            level++;
        }
        owner.setMembershipLevel(Math.min(level, MAX_LEVEL));
    }

    private static boolean hasEmail(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isEmpty();
    }

    private static boolean hasUniqueName(Owner owner) {
        return owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
    }

    /** Days the owner has been registered as of today; zero (or negative) for a new owner. */
    private static long tenureDays(LocalDate registrationDate) {
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
    }
}

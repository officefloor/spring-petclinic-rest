package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs in {@code POST /api/owners} after the owner's household size and namesake count have been
 * recorded and before it is saved, within the create transaction. Records on the built owner its
 * numeric membership points: starting at 0, it gains 2 when an email is present, 1 more when
 * {@link Owner#getNamesakeCount() namesakeCount} is 0, 2 for a household of
 * {@link #LARGE_HOUSEHOLD_MIN} or more members, and 3 when the owner's tenure exceeds
 * {@link #TENURE_DAYS_FOR_BONUS} days. Because a newly registered owner has zero tenure, a new
 * owner never earns the tenure points. Reads the resolved (weekend-adjusted) registration date
 * published by {@link ResolveOwnerRegistrationDate} — the owner is not stamped with it until
 * {@link ApplyOwnerRegistrationDate}, which runs later. {@link AssignOwnerMembershipLevel} maps the
 * points recorded here onto the membership level.
 */
public class AssignOwnerMembershipPoints {

    /** Points earned when the owner has an email address. */
    private static final int EMAIL_POINTS = 2;

    /** Points earned when no existing owner shared the owner's name at creation. */
    private static final int UNIQUE_NAME_POINTS = 1;

    /** Points earned when the owner belongs to a household of {@link #LARGE_HOUSEHOLD_MIN}+. */
    private static final int LARGE_HOUSEHOLD_POINTS = 2;

    /** Smallest household size that earns {@link #LARGE_HOUSEHOLD_POINTS}. */
    private static final int LARGE_HOUSEHOLD_MIN = 3;

    /** Points earned when the owner's tenure exceeds {@link #TENURE_DAYS_FOR_BONUS} days. */
    private static final int LONG_TENURE_POINTS = 3;

    /** Tenure, in days, an owner must exceed to earn {@link #LONG_TENURE_POINTS}. */
    private static final long TENURE_DAYS_FOR_BONUS = 365;

    public void service(@Val Owner owner, @Val LocalDate registrationDate) {
        int points = 0;
        if (hasEmail(owner)) {
            points += EMAIL_POINTS;
        }
        if (hasUniqueName(owner)) {
            points += UNIQUE_NAME_POINTS;
        }
        if (hasLargeHousehold(owner)) {
            points += LARGE_HOUSEHOLD_POINTS;
        }
        if (tenureDays(registrationDate) > TENURE_DAYS_FOR_BONUS) {
            points += LONG_TENURE_POINTS;
        }
        owner.setMembershipPoints(points);
    }

    private static boolean hasEmail(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isEmpty();
    }

    private static boolean hasUniqueName(Owner owner) {
        return owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
    }

    private static boolean hasLargeHousehold(Owner owner) {
        return owner.getHouseholdSize() != null && owner.getHouseholdSize() >= LARGE_HOUSEHOLD_MIN;
    }

    /** Days the owner has been registered as of today; zero (or negative) for a new owner. */
    private static long tenureDays(LocalDate registrationDate) {
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
    }
}

package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Scores the owner's {@code membershipPoints} at creation, then maps them to a numeric
 * {@code membershipLevel} (1 to 4).
 *
 * <p>Points start at {@code 0}; add {@code 2} when an email is present; add {@code 1} when
 * {@code namesakeCount} is {@code 0}; add {@code 2} for a household of {@code 3} or more; add
 * {@code 3} when tenure exceeds {@code 365} days.
 *
 * <p>Points map to level as: 1 for 0-1 points, 2 for 2-3 points, 3 for 4-5 points, 4 for 6 or
 * more points.
 *
 * <p>Level 4 requires 6 points, and the only route to 6 is the tenure factor (+3). A newly
 * created owner has zero tenure (its registration date is today), so the tenure factor never
 * applies at creation and a new owner never exceeds level 3.
 *
 * <p>Runs after {@link AssignNamesakeCount} and {@link AssignHouseholdSize} (so those counts are
 * set) and before {@link SaveOwner}, so both fields are persisted and available to the create
 * audit line.
 */
public class AssignMembershipLevel {

    /** Tenure threshold, in days, above which the tenure factor is awarded. */
    private static final long TENURE_DAYS_THRESHOLD = 365;

    public void service(@Val Owner owner) {
        int points = 0;
        String email = owner.getEmail();
        if (email != null && !email.isBlank()) {
            points += 2;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            points += 1;
        }
        Integer householdSize = owner.getHouseholdSize();
        if (householdSize != null && householdSize >= 3) {
            points += 2;
        }
        if (tenureDays(owner) > TENURE_DAYS_THRESHOLD) {
            points += 3;
        }
        owner.setMembershipPoints(points);
        owner.setMembershipLevel(levelForPoints(points));
    }

    /** Maps points to level: 1 (0-1), 2 (2-3), 3 (4-5), 4 (6 or more). */
    private static int levelForPoints(int points) {
        if (points >= 6) {
            return 4;
        }
        if (points >= 4) {
            return 3;
        }
        if (points >= 2) {
            return 2;
        }
        return 1;
    }

    /** Days of tenure since registration; zero (or negative) for a newly registered owner. */
    private static long tenureDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
    }
}

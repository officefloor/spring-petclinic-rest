package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the {@code membershipPoints} score and the derived numeric {@code membershipLevel} at
 * creation time. Points start at {@code 0} and accrue: {@code 2} when the owner has an email,
 * {@code 1} when {@code namesakeCount} is {@code 0}, {@code 2} for a household of {@code 3} or more
 * (from {@code householdSize}), and {@code 3} for tenure over {@code 365} days (days between the
 * owner's {@code registrationDate} and the current server date). Points map to a level as
 * {@code 1} (0-1 points), {@code 2} (2-3), {@code 3} (4-5) and {@code 4} (6 or more). Runs after
 * {@link CountNamesakes} and {@link CountHousehold} so both counts are already set.
 */
public class AssignMembershipLevel {

    /** Tenure, in days, above which the owner qualifies for the tenure points. */
    private static final long TENURE_THRESHOLD_DAYS = 365;

    /** Household size at or above which the household points are awarded. */
    private static final int HOUSEHOLD_THRESHOLD = 3;

    public void service(@Val Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= HOUSEHOLD_THRESHOLD) {
            points += 2;
        }
        if (tenureDays(owner) > TENURE_THRESHOLD_DAYS) {
            points += 3;
        }
        owner.setMembershipPoints(points);
        owner.setMembershipLevel(levelForPoints(points));
    }

    private static int levelForPoints(int points) {
        if (points <= 1) {
            return 1;
        }
        if (points <= 3) {
            return 2;
        }
        if (points <= 5) {
            return 3;
        }
        return 4;
    }

    private static long tenureDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
    }
}

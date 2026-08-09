package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the new owner's {@code membershipPoints} and derived {@code membershipLevel} at creation.
 * Points start at 0 and accumulate: +2 when an email is present, +1 when the owner's
 * {@code namesakeCount} is 0, +2 for a household of 3 or more (its {@code householdSize}) and +3 for
 * tenure over 365 days (the days elapsed since the owner's {@code registrationDate}). The points are
 * then mapped to a level: 1 for 0-1 points, 2 for 2-3, 3 for 4-5 and 4 for 6 or more. Both fields are
 * stored on the owner.
 *
 * <p>Runs after {@link AssignHouseholdSize} and {@link AssignNamesakeCount} (so the household and
 * namesake snapshots exist) and before {@link SaveOwner}, mutating the not-yet-persisted owner in
 * place.
 */
public class AssignMembershipLevel {

    private static final long TENURE_DAYS_FOR_POINTS = 365;

    public void service(@Val Owner owner) {
        int points = 0;

        String email = owner.getEmail();
        if (email != null && !email.isEmpty()) {
            points += 2;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            points += 1;
        }
        Integer householdSize = owner.getHouseholdSize();
        if (householdSize != null && householdSize >= 3) {
            points += 2;
        }
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate != null) {
            long tenureDays = ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
            if (tenureDays > TENURE_DAYS_FOR_POINTS) {
                points += 3;
            }
        }

        owner.setMembershipPoints(points);
        owner.setMembershipLevel(levelFor(points));
    }

    private static int levelFor(int points) {
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
}

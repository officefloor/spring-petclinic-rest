package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns, on the owner being created, the membership points and the numeric membership level derived
 * from them. Points start at zero and gain: 2 when an email is present, 1 when the owner has no
 * pre-existing namesakes ({@code namesakeCount} of zero), 2 for a household of three or more, and 3 for
 * a tenure of more than 365 days measured from the owner's {@code registrationDate}. The points map to a
 * level: 1 for 0-1 points, 2 for 2-3, 3 for 4-5 and 4 for 6 or more. Runs after
 * {@link NormalizeOwnerEmail}, {@link AssignNamesakeCount} and {@link AssignHouseholdSize} so all inputs
 * are settled, and before {@link SaveOwner} persists the values.
 */
public class AssignMembershipLevel {

    /** A tenure strictly greater than this many days scores the tenure points. */
    private static final long TENURE_POINTS_DAYS = 365;

    public void service(@Val Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3) {
            points += 2;
        }
        if (tenureDays(owner) > TENURE_POINTS_DAYS) {
            points += 3;
        }
        owner.setMembershipPoints(points);
        owner.setMembershipLevel(levelOf(points));
    }

    private static int levelOf(int points) {
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

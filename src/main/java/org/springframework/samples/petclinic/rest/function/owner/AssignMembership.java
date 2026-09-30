package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.FiscalYear;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code membershipPoints} and the {@code membershipLevel} derived
 * from them, both fixed at create time. A pure calculation over values already assigned to
 * the owner (email, namesakeCount, registrationDate) plus the household size published by
 * {@link AssignHouseholdSize} — it computes nothing itself.
 *
 * <p>Points start at 0 and accumulate: 2 when an email is present, 1 when the owner has no
 * namesakes ({@code namesakeCount} of 0), 2 when the owner belongs to a household of 3 or
 * more, and 3 when the owner's tenure is more than one elapsed fiscal year. Tenure is the
 * number of whole fiscal years elapsed between the registration date and today; a newly
 * created owner has zero tenure.
 *
 * <p>The points map to a level: 0-1 points is level 1, 2-3 is level 2, 4-5 is level 3, and
 * 6 or more is level 4.
 */
public class AssignMembership {

    static final int POINTS_EMAIL = 2;
    static final int POINTS_NO_NAMESAKE = 1;
    static final int POINTS_LARGE_HOUSEHOLD = 2;
    static final int POINTS_LONG_TENURE = 3;

    /** A household of this size or more earns {@link #POINTS_LARGE_HOUSEHOLD}. */
    static final int LARGE_HOUSEHOLD_SIZE = 3;

    /** Long-tenure points require tenure of more than this many elapsed fiscal years. */
    static final int TENURE_FISCAL_YEARS_FOR_LONG_TENURE = 1;

    public void service(@Val Owner owner, @Qualified("householdSize") @Val Integer householdSize) {
        int points = 0;

        String email = owner.getEmail();
        if (email != null && !email.isBlank()) {
            points += POINTS_EMAIL;
        }

        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            points += POINTS_NO_NAMESAKE;
        }

        if (householdSize != null && householdSize >= LARGE_HOUSEHOLD_SIZE) {
            points += POINTS_LARGE_HOUSEHOLD;
        }

        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate != null
                && FiscalYear.elapsedYears(registrationDate, LocalDate.now()) > TENURE_FISCAL_YEARS_FOR_LONG_TENURE) {
            points += POINTS_LONG_TENURE;
        }

        owner.setMembershipPoints(points);
        owner.setMembershipLevel(levelForPoints(points));
    }

    /** Maps points to a level: 0-1 -> 1, 2-3 -> 2, 4-5 -> 3, 6+ -> 4. */
    private int levelForPoints(int points) {
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

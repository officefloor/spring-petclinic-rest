package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's numeric {@code membershipLevel} (1 to 4), scored at creation:
 * start at {@code 1}; add {@code 1} when an email is present; add {@code 1} when
 * {@code namesakeCount} is {@code 0}; add {@code 1} when tenure exceeds {@code 365} days;
 * capped at {@code 4}.
 *
 * <p>Level 4 requires tenure of more than 365 days. A newly created owner has zero tenure
 * (its registration date is today), so the tenure factor never applies at creation and a new
 * owner never exceeds level 3.
 *
 * <p>Runs after {@link AssignNamesakeCount} (so the namesake count is set) and before
 * {@link SaveOwner}, so the level is persisted and available to the create audit line.
 */
public class AssignMembershipLevel {

    /** Tenure threshold, in days, above which the tenure factor is awarded. */
    private static final long TENURE_DAYS_THRESHOLD = 365;

    public void service(@Val Owner owner) {
        int level = 1;
        String email = owner.getEmail();
        if (email != null && !email.isBlank()) {
            level++;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            level++;
        }
        if (tenureDays(owner) > TENURE_DAYS_THRESHOLD) {
            level++;
        }
        owner.setMembershipLevel(Math.min(level, 4));
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

package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns, on the owner being created, the numeric membership level. The level starts at one, gains a
 * point when an email is present and a further point when the owner has no pre-existing namesakes
 * ({@code namesakeCount} of zero); these pre-tenure factors are capped at three. Level four requires
 * a tenure of more than 365 days, measured from the owner's {@code registrationDate}. A newly created
 * owner has zero tenure, so a new owner never exceeds level three. Runs after
 * {@link NormalizeOwnerEmail} and {@link AssignNamesakeCount} so both inputs are settled, and before
 * {@link SaveOwner} persists the value.
 */
public class AssignMembershipLevel {

    /** A tenure strictly greater than this many days qualifies for level four. */
    private static final long LEVEL_FOUR_TENURE_DAYS = 365;

    public void service(@Val Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        level = Math.min(level, 3);
        if (tenureDays(owner) > LEVEL_FOUR_TENURE_DAYS) {
            level = 4;
        }
        owner.setMembershipLevel(level);
    }

    private static long tenureDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
    }
}

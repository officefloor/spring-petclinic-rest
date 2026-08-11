package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric {@code membershipLevel} from the owner's state.
 *
 * <p>The level starts at 1 and is incremented by the owner's standing:
 *
 * <ul>
 *   <li>+1 when the owner has a non-blank email;</li>
 *   <li>+1 when the owner has no namesakes ({@code namesakeCount == 0}).</li>
 * </ul>
 *
 * <p>Those factors alone top out at level 3. Level 4 additionally requires tenure of more than
 * 365 days &mdash; the number of days from the owner's {@code registrationDate} to today. Because a
 * newly created owner registers today, its tenure is zero, so a new owner never exceeds level 3
 * (a new owner with an email and {@code namesakeCount == 0} lands at level 3, not 4).
 */
public final class MembershipLevels {

    private static final long LEVEL_4_TENURE_DAYS = 365;

    private MembershipLevels() {
    }

    public static int levelFor(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        level = Math.min(level, 3);
        // Level 4 is reserved for owners whose tenure exceeds 365 days.
        if (level == 3 && tenureDays(owner) > LEVEL_4_TENURE_DAYS) {
            level++;
        }
        return level;
    }

    private static long tenureDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
    }
}

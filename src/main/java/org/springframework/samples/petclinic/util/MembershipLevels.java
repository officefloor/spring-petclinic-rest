package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code membershipPoints} and numeric {@code membershipLevel} from the owner's
 * state.
 *
 * <p>Points start at 0 and accumulate from the owner's standing:
 *
 * <ul>
 *   <li>+2 when the owner has a non-blank email;</li>
 *   <li>+1 when the owner has no namesakes ({@code namesakeCount == 0});</li>
 *   <li>+2 for a household of 3 or more members ({@code householdMemberCount >= 3});</li>
 *   <li>+3 for tenure over 365 days &mdash; the number of days from the owner's
 *       {@code registrationDate} to today.</li>
 * </ul>
 *
 * <p>The points map to a level: 1 (0&ndash;1 points), 2 (2&ndash;3), 3 (4&ndash;5), 4 (6 or more).
 * Because a newly created owner registers today, its tenure is zero, so a new owner never earns the
 * tenure points.
 */
public final class MembershipLevels {

    private static final long TENURE_POINTS_DAYS = 365;

    private MembershipLevels() {
    }

    public static int pointsFor(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (owner.getHouseholdMemberCount() != null && owner.getHouseholdMemberCount() >= 3) {
            points += 2;
        }
        if (tenureDays(owner) > TENURE_POINTS_DAYS) {
            points += 3;
        }
        return points;
    }

    public static int levelFor(Owner owner) {
        int points = pointsFor(owner);
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

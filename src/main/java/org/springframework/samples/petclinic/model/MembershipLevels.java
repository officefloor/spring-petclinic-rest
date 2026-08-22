package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Derives an owner's membership points and the numeric membership level those points map
 * to. Kept as a plain static helper so the single rule is shared by the response mapper
 * and the create audit line.
 */
public final class MembershipLevels {

    private MembershipLevels() {
    }

    /**
     * The membership points: start at 0, add 2 when an email is present, add 1 when
     * namesakeCount is 0, add 2 for a household of 3 or more, add 3 for tenure over 365
     * days. A newly created owner has zero tenure, so it cannot earn the tenure points on
     * creation.
     */
    public static int points(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3) {
            points += 2;
        }
        if (tenureDays(owner) > 365) {
            points += 3;
        }
        return points;
    }

    /**
     * The membership level from 1 to 4, mapped from {@link #points(Owner)}: 1 for 0-1
     * points, 2 for 2-3, 3 for 4-5, 4 for 6 or more.
     */
    public static int of(Owner owner) {
        int points = points(owner);
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

    /**
     * Days since the owner registered, or 0 when the registration date is unknown. A new
     * owner registers today, so its tenure is 0.
     */
    private static long tenureDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
    }
}

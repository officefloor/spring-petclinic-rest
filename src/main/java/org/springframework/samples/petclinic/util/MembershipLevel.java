package org.springframework.samples.petclinic.util;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Maps an owner's {@link MembershipPoints} score to a numeric membership level: level 1
 * for 0-1 points, level 2 for 2-3, level 3 for 4-5, and level 4 for 6 or more points.
 */
public final class MembershipLevel {

    /** Lowest level attainable. */
    public static final int MIN = 1;

    /** Highest level attainable. */
    public static final int MAX = 4;

    private MembershipLevel() {
    }

    /** The membership level for the given owner as of today. */
    public static int of(Owner owner) {
        return of(owner, LocalDate.now());
    }

    /** The membership level for the given owner, with tenure measured as of {@code asOf}. */
    public static int of(Owner owner, LocalDate asOf) {
        return forPoints(MembershipPoints.of(owner, asOf));
    }

    /** The membership level for a given membership points score. */
    public static int forPoints(int points) {
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
}

package org.springframework.samples.petclinic.util;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Maps an owner's {@link MembershipPoints} score to a numeric membership level: level 1
 * for 0-1 points, level 2 for 2-3, level 3 for 4-5, and level 4 for 6 or more points.
 *
 * <p>An owner may carry a household level ceiling ({@link Owner#getMembershipLevelCap()});
 * when present it caps the effective level, so a new owner never outranks the highest of
 * their existing household members by more than one.
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

    /** The membership level for the given owner, with tenure measured as of {@code asOf}.
     *  Limited by the owner's stored household level ceiling when one is set. */
    public static int of(Owner owner, LocalDate asOf) {
        return capped(forPoints(MembershipPoints.of(owner, asOf)), owner.getMembershipLevelCap());
    }

    /** Apply a household level ceiling: {@code cap} (when non-null) limits {@code level}. */
    private static int capped(int level, Integer cap) {
        return cap == null ? level : Math.min(level, cap);
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

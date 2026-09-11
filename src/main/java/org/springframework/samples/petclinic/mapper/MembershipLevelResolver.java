package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Derives an owner's numeric 'membershipLevel', assigned on create. Kept out of
 * {@link OwnerMapper} so MapStruct does not mistake it for an implicit property
 * mapping method.
 */
public final class MembershipLevelResolver {

    /** Highest level attainable without tenure; level 4 is gated on tenure. */
    private static final int PRE_TENURE_MAX_LEVEL = 3;

    /** Highest level attainable at all, once the tenure requirement is met. */
    private static final int TENURED_MAX_LEVEL = 4;

    /** Tenure, in days, that must be exceeded before level 4 may be awarded. */
    private static final int TENURE_THRESHOLD_DAYS = 365;

    /** A household of this many members (or more) earns the GOLD (level 4) factor. */
    private static final int GOLD_HOUSEHOLD_SIZE = 3;

    private MembershipLevelResolver() {
    }

    /**
     * Returns the membership level for an owner. Starts at 1, gains 1 when an
     * email is present (non-blank), gains 1 when the owner's name was unique on
     * create ({@code namesakeCount} is 0) and gains 1 when the owner belongs to a
     * household of three or more members (the GOLD factor).
     *
     * <p>Level 4 is reserved for tenured owners: it is awarded only when the owner
     * has been registered for more than 365 days. Below that tenure the level is
     * capped at 3, so a newly created owner (zero tenure) never exceeds level 3 even
     * with an email, a unique name and a three-member household.
     *
     * @param email                the owner's email, may be {@code null}
     * @param namesakeCount        the owner's namesake count, may be {@code null}
     * @param householdMemberCount the number of owners in the owner's household, may be {@code null}
     * @param registrationDate     the owner's registration date, may be {@code null}
     * @return the membership level, between 1 and 4 inclusive
     */
    public static int deriveMembershipLevel(String email, Integer namesakeCount,
            Integer householdMemberCount, LocalDate registrationDate) {
        int level = 1;
        if (email != null && !email.isBlank()) {
            level++;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        if (householdMemberCount != null && householdMemberCount >= GOLD_HOUSEHOLD_SIZE) {
            level++;
        }
        int maxLevel = hasTenure(registrationDate) ? TENURED_MAX_LEVEL : PRE_TENURE_MAX_LEVEL;
        return Math.min(level, maxLevel);
    }

    /**
     * Reports whether an owner's tenure exceeds the level-4 threshold, i.e. it has been more than
     * 365 days since {@code registrationDate}. A {@code null} or future registration date counts as
     * no tenure.
     *
     * @param registrationDate the owner's registration date, may be {@code null}
     * @return {@code true} when the owner has been registered for more than 365 days
     */
    private static boolean hasTenure(LocalDate registrationDate) {
        if (registrationDate == null) {
            return false;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now()) > TENURE_THRESHOLD_DAYS;
    }
}

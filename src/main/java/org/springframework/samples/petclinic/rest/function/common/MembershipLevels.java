package org.springframework.samples.petclinic.rest.function.common;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric membership level (1 to 4) from stable owner fields,
 * replacing the former string membership tier.
 *
 * <p>The level starts at 1 and gains one for each pre-tenure factor: the owner has an
 * email, the owner's {@code namesakeCount} is 0, and the owner belongs to a household
 * of three or more members ({@code householdSize} of 3 or more). Those factors alone
 * cap the level at {@link #MAX_PRE_TENURE_LEVEL} (3).
 *
 * <p>Level {@link #TOP_LEVEL} (4) additionally requires a tenure of more than
 * {@link #TENURE_DAYS_FOR_TOP_LEVEL} days measured from the registration date. Because a
 * newly created owner has zero tenure, a new owner never exceeds level 3 — even one with
 * an email, a {@code namesakeCount} of 0 and a three-member household is level 3, not 4.
 */
public final class MembershipLevels {

    /** The highest level derivable from the pre-tenure factors alone. */
    public static final int MAX_PRE_TENURE_LEVEL = 3;

    /** The top level, reachable only with sufficient tenure. */
    public static final int TOP_LEVEL = 4;

    /** Level {@link #TOP_LEVEL} requires strictly more than this many days of tenure. */
    public static final int TENURE_DAYS_FOR_TOP_LEVEL = 365;

    private MembershipLevels() {
    }

    /**
     * Compute the membership level for the given owner.
     */
    public static int of(Owner owner) {
        int level = 1;
        String email = owner.getEmail();
        if (email != null && !email.isBlank()) {
            level++;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        Integer householdSize = owner.getHouseholdSize();
        if (householdSize != null && householdSize >= 3) {
            level++;
        }
        int cap = hasTopLevelTenure(owner) ? TOP_LEVEL : MAX_PRE_TENURE_LEVEL;
        return Math.min(level, cap);
    }

    private static boolean hasTopLevelTenure(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return false;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now()) > TENURE_DAYS_FOR_TOP_LEVEL;
    }
}

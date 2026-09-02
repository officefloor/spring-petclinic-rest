package org.springframework.samples.petclinic.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: on creation an owner is assigned a numeric {@code membershipLevel} from 1 to 4.
 * It starts at 1, gains 1 when an email is present, gains 1 when {@code namesakeCount} is 0, and is
 * capped at 3 for the pre-tenure factors; level 4 is reserved for tenure of more than 365 days, so a
 * newly created owner (zero tenure) never exceeds level 3. Kept as a small, self-contained unit so
 * the rule can be applied from the read and audit flows without adding complexity to the mapper,
 * controller, or service.
 */
public final class OwnerMembershipLevelPolicy {

    private static final int MAX_LEVEL = 3;

    private static final long TENURE_DAYS = 365;

    private OwnerMembershipLevelPolicy() {
    }

    /**
     * Derive the {@code membershipLevel} for the given owner.
     *
     * @param owner the owner whose membership level to derive
     * @return the level, from 1 to 4
     */
    public static int membershipLevel(Owner owner) {
        int level = 1;
        String email = owner.getEmail();
        if (email != null && !email.isEmpty()) {
            level++;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        level = Math.min(level, MAX_LEVEL);
        return level == MAX_LEVEL && exceedsTenure(owner.getRegistrationDate()) ? level + 1 : level;
    }

    /** True when tenure (days since registration) is more than the level-4 threshold. */
    private static boolean exceedsTenure(LocalDate registrationDate) {
        return registrationDate != null
            && ChronoUnit.DAYS.between(registrationDate, LocalDate.now()) > TENURE_DAYS;
    }
}

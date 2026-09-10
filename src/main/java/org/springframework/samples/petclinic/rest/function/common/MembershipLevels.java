package org.springframework.samples.petclinic.rest.function.common;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric membership level (1 to 3) from stable owner fields,
 * replacing the former string membership tier.
 *
 * <p>The level starts at 1, gains 1 when the owner has an email, gains 1 when the
 * owner's {@code namesakeCount} is 0, and is capped at 3. Level 4 is reserved for
 * tenure. Because it depends only on the persisted email and namesakeCount, the
 * create response and any later GET compute the same value.
 */
public final class MembershipLevels {

    /** The highest level derivable at creation; level 4 is reserved for tenure. */
    public static final int MAX_LEVEL = 3;

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
        return Math.min(level, MAX_LEVEL);
    }
}

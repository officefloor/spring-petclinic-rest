package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric membership level (1 to 3) from the state captured
 * when the owner was created. Kept as a plain static helper - rather than a
 * method on {@link OwnerMapper} - so MapStruct does not mistake it for an
 * implicit property mapping.
 *
 * <p>The level starts at 1, gains 1 when an email address is present, gains a
 * further 1 when the name was unique on create ({@code namesakeCount} is 0), and
 * is capped at 3. Level 4 is reserved for tenure and is never assigned here.
 */
public final class MembershipLevel {

    /** The lowest membership level, assigned to every owner. */
    private static final int BASE_LEVEL = 1;

    /** The highest level assignable on creation; level 4 is reserved for tenure. */
    private static final int MAX_LEVEL = 3;

    private MembershipLevel() {
    }

    /**
     * Computes the owner's membership level: 1, plus 1 when an email address is
     * present, plus 1 when {@code namesakeCount} is 0, capped at {@link #MAX_LEVEL}.
     *
     * @param owner the owner whose level to derive
     * @return the numeric membership level between 1 and 3 inclusive
     */
    public static int forOwner(Owner owner) {
        int level = BASE_LEVEL;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        return Math.min(level, MAX_LEVEL);
    }

}

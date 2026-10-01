package org.springframework.samples.petclinic.model;

/**
 * Derives an owner's numeric membership level, assigned on create.
 *
 * <p>The level starts at 1, gains 1 when an email is present, gains 1 when the owner's
 * {@code namesakeCount} is 0 (the name was unique on create) and is capped at {@link #MAX} (3).
 * Level 4 is reserved for tenure and is not awarded here.
 *
 * <p>This replaces the former string membership tier and is the single source of truth for the
 * computation shared by {@code OwnerMapper} (the {@code membershipLevel} response field) and the
 * create audit line.
 */
public final class MembershipLevel {

    /** The highest level this rule awards; level 4 is reserved for tenure. */
    public static final int MAX = 3;

    private MembershipLevel() {
    }

    /**
     * Compute the membership level for an owner.
     *
     * @param owner the owner; its {@code email} and {@code namesakeCount} drive the result
     * @return the level, between 1 and {@link #MAX} inclusive
     */
    public static int of(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        return Math.min(level, MAX);
    }
}

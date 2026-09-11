package org.springframework.samples.petclinic.model;

/**
 * Derives an owner's numeric membership level, assigned at creation.
 *
 * <p>Starts at 1; add 1 when an email is present; add 1 when {@code namesakeCount}
 * is 0; capped at 3. Level 4 is reserved for tenure.
 */
public final class MembershipLevel {

    /** The maximum level assignable at creation; level 4 is reserved for tenure. */
    public static final int MAX = 3;

    private MembershipLevel() {
    }

    /** The membership level for the given owner. */
    public static int of(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        return Math.min(level, MAX);
    }
}

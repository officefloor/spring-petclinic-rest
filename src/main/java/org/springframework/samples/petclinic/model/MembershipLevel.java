package org.springframework.samples.petclinic.model;

/**
 * Derives an owner's numeric membership level, assigned once at creation.
 *
 * <p>Starts at {@code 1}; adds {@code 1} when an email address is present; adds {@code 1} when the
 * owner has no namesakes ({@code namesakeCount} is {@code 0}); the result is capped at {@code 3}.
 * Level {@code 4} is reserved for tenure and is never produced here.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    /** Compute the membership level (1..3) for the given owner. */
    public static int of(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        return Math.min(level, 3);
    }
}

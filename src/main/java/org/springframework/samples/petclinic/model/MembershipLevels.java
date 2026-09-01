package org.springframework.samples.petclinic.model;

/**
 * Derives an owner's numeric membership level: starts at 1, plus 1 when an
 * email is present and plus 1 when namesakeCount is 0, capped at 3. Level 4 is
 * reserved for tenure.
 */
public final class MembershipLevels {

    private MembershipLevels() {
    }

    /** Membership level (1 to 3) for {@code owner}. */
    public static int levelOf(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        return Math.min(level, 3);
    }
}

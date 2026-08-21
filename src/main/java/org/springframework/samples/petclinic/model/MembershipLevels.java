package org.springframework.samples.petclinic.model;

/**
 * Derives an owner's numeric membership level. Kept as a plain static helper so the
 * single rule is shared by the response mapper and the create audit line.
 */
public final class MembershipLevels {

    private MembershipLevels() {
    }

    /**
     * The membership level from 1 to 3: start at 1, add 1 when an email is present, add 1
     * when namesakeCount is 0, capped at 3 (level 4 is reserved for tenure).
     */
    public static int of(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        return Math.min(level, 3);
    }
}

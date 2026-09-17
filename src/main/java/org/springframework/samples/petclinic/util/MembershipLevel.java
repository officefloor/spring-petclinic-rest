package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's membership level, a number from 1 to 3 fixed at the moment the owner is
 * created. It starts at 1, gains 1 when an email address is present, and gains 1 when the owner has
 * a unique name (namesakeCount is zero), capped at 3. Level 4 is reserved for tenure. Pure function
 * of the owner's own fields, so it is derived at response time rather than stored on the entity.
 */
public final class MembershipLevel {

    /** The lowest membership level every owner starts from. */
    public static final int BASE = 1;

    /** The highest level this rule awards; level 4 is reserved for tenure. */
    public static final int CAP = 3;

    private MembershipLevel() {
    }

    /** The membership level for {@code owner}, or {@code null} when the owner is {@code null}. */
    public static Integer of(Owner owner) {
        if (owner == null) {
            return null;
        }
        int level = BASE;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        return Math.min(level, CAP);
    }
}

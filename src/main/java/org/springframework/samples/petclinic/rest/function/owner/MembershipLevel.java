package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Membership level rule: a numeric level from 1 to 3 assigned on creation. It starts at 1, gains
 * 1 when the owner has an email address, gains 1 when the owner has no namesakes, and is capped at
 * 3 — level 4 is reserved for tenure.
 */
public final class MembershipLevel {

    private static final int CAP = 3;

    private MembershipLevel() {
    }

    public static int of(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        return Math.min(level, CAP);
    }
}

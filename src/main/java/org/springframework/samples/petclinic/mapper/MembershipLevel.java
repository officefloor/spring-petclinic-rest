package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric membership level (1 to 3) from their persisted fields.
 *
 * <p>Starts at 1; adds 1 when an email is present; adds 1 when {@code namesakeCount} is 0;
 * capped at 3 (level 4 is reserved for tenure).
 *
 * <p>Kept as a standalone class (not a method on {@link OwnerMapper}) so MapStruct does not
 * mistake it for an implicit mapping method. Single source of truth so the response DTO and
 * the create audit line agree on the level.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    /** The membership level (1 to 3) for {@code owner}. */
    public static int of(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        return Math.min(3, level);
    }
}

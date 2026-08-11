package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric {@code membershipLevel} (1..3). The level starts at 1,
 * gains 1 when the owner has a non-blank email, gains 1 when {@code namesakeCount} is 0,
 * and is capped at 3 (level 4 is reserved for tenure). Kept out of {@link OwnerMapper}
 * so MapStruct does not mistake the helper for an implicit mapping method.
 */
public final class Membership {

    private Membership() {
    }

    /** The numeric membership level (1..3) for {@code owner}. */
    public static int levelOf(Owner owner) {
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

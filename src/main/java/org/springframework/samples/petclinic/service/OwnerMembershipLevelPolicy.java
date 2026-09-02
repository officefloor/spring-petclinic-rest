package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: on creation an owner is assigned a numeric {@code membershipLevel} from 1 to 3.
 * It starts at 1, gains 1 when an email is present, gains 1 when {@code namesakeCount} is 0, and is
 * capped at 3 (level 4 is reserved for tenure). Kept as a small, self-contained unit so the rule can
 * be applied from the read and audit flows without adding complexity to the mapper, controller, or
 * service.
 */
public final class OwnerMembershipLevelPolicy {

    private static final int MAX_LEVEL = 3;

    private OwnerMembershipLevelPolicy() {
    }

    /**
     * Derive the {@code membershipLevel} for the given owner.
     *
     * @param owner the owner whose membership level to derive
     * @return the level, from 1 to 3
     */
    public static int membershipLevel(Owner owner) {
        int level = 1;
        String email = owner.getEmail();
        if (email != null && !email.isEmpty()) {
            level++;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        return Math.min(level, MAX_LEVEL);
    }
}

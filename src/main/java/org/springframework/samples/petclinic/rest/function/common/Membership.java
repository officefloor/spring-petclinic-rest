package org.springframework.samples.petclinic.rest.function.common;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the owner's numeric {@code membershipLevel}, fixed by the facts known at creation.
 *
 * <p>The level starts at 1, gains 1 when the owner has an email address on record, gains a
 * further 1 when the owner had no namesakes at creation ({@code namesakeCount} is 0), and is
 * capped at 3. Level 4 is reserved for tenure and is never assigned here.
 */
public final class Membership {

    private Membership() {
    }

    /** The owner's numeric membership level, from 1 to 3. */
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

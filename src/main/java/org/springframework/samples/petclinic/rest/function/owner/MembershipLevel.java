package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code membershipLevel} — the numeric rank (1 to 3) returned on the owner DTO
 * and recorded on the create audit line.
 *
 * <p>The level is fixed at creation from the owner's own fields:
 * <ul>
 *   <li>starts at {@code 1};</li>
 *   <li>adds {@code 1} when an email address is present (non-blank);</li>
 *   <li>adds {@code 1} when the owner's name is unique ({@code namesakeCount == 0});</li>
 *   <li>capped at {@code 3} — level 4 is reserved for tenure.</li>
 * </ul>
 */
public final class MembershipLevel {

    private static final int MAX_LEVEL = 3;

    private MembershipLevel() {
    }

    /** The owner's membership level, from 1 to 3. */
    public static int of(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        return Math.min(level, MAX_LEVEL);
    }
}

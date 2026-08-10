package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric {@code membershipLevel} from the owner's state as fixed at creation.
 *
 * <p>The level starts at 1 and is incremented by the owner's standing:
 *
 * <ul>
 *   <li>+1 when the owner has a non-blank email;</li>
 *   <li>+1 when the owner has no namesakes ({@code namesakeCount == 0}).</li>
 * </ul>
 *
 * <p>The result is capped at 3. Level 4 is reserved for tenure and is not assigned here.
 */
public final class MembershipLevels {

    private MembershipLevels() {
    }

    public static int levelFor(Owner owner) {
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

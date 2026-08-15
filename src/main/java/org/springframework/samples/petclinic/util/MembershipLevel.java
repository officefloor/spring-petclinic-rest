package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric {@code membershipLevel} (1 to 3) from its stored fields. The level is
 * fixed by the values captured at creation: it starts at 1, gains 1 when the owner has an email,
 * gains 1 when its {@code namesakeCount} is 0, and is capped at 3. Level 4 is reserved for tenure
 * and is never produced here.
 */
public final class MembershipLevel {

    /** The lowest level every owner starts at. */
    private static final int BASE = 1;

    /** The highest level this rule may award; level 4 is reserved for tenure. */
    private static final int CAP = 3;

    private MembershipLevel() {
    }

    /** Returns the membership level (1 to 3) for {@code owner} from its stored fields. */
    public static int levelOf(Owner owner) {
        int level = BASE;
        String email = owner.getEmail();
        if (email != null && !email.isEmpty()) {
            level++;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        return Math.min(level, CAP);
    }
}

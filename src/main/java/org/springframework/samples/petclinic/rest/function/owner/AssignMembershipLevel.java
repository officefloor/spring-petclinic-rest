package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code membershipLevel}, a numeric level from 1 to 3 fixed at
 * create time: it starts at 1, gains 1 when an email is present and 1 more when the
 * owner has no namesakes ({@code namesakeCount} of 0), capped at 3. Level 4 is reserved
 * for tenure and is never assigned here. Runs after the namesake count has been assigned,
 * so that value is available.
 */
public class AssignMembershipLevel {

    static final int MAX_LEVEL = 3;

    public void service(@Val Owner owner) {
        int level = 1;
        String email = owner.getEmail();
        if (email != null && !email.isBlank()) {
            level++;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        owner.setMembershipLevel(Math.min(level, MAX_LEVEL));
    }
}

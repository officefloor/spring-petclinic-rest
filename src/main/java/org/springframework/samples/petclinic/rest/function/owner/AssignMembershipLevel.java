package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.util.StringUtils;

/**
 * Assigns a newly built owner its numeric {@code membershipLevel}. Starting from 1, the level
 * gains a point when the owner has an email address and another when it had no namesakes at
 * creation ({@link Owner#getNamesakeCount()} is 0), capped at 3. Level 4 is reserved for tenure
 * and so is never assigned here. Runs after {@link CountNamesakes} so the namesake count is set.
 */
public class AssignMembershipLevel {

    /** The highest membership level assignable on creation; level 4 is reserved for tenure. */
    private static final int MAX_LEVEL_ON_CREATION = 3;

    public void service(@Val Owner owner) {
        int level = 1;
        if (StringUtils.hasText(owner.getEmail())) {
            level++;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            level++;
        }
        owner.setMembershipLevel(Math.min(level, MAX_LEVEL_ON_CREATION));
    }
}

package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the new owner's numeric {@code membershipLevel} at creation. The level starts at 1,
 * gains 1 when an email is present and 1 more when the owner's {@code namesakeCount} is 0, and is
 * capped at 3 (level 4 is reserved for tenure).
 *
 * <p>Runs after {@link AssignNamesakeCount} (so the namesake snapshot exists) and before
 * {@link SaveOwner}, mutating the not-yet-persisted owner in place.
 */
public class AssignMembershipLevel {

    public void service(@Val Owner owner) {
        int level = 1;
        String email = owner.getEmail();
        if (email != null && !email.isEmpty()) {
            level++;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        owner.setMembershipLevel(Math.min(level, 3));
    }
}

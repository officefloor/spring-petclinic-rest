package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns, on the owner being created, the numeric membership level. The level starts at one, gains a
 * point when an email is present and a further point when the owner has no pre-existing namesakes
 * ({@code namesakeCount} of zero), and is capped at three. Level four is reserved for tenure and is
 * never assigned at creation. Runs after {@link NormalizeOwnerEmail} and {@link AssignNamesakeCount}
 * so both inputs are settled, and before {@link SaveOwner} persists the value.
 */
public class AssignMembershipLevel {

    public void service(@Val Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        owner.setMembershipLevel(Math.min(level, 3));
    }
}

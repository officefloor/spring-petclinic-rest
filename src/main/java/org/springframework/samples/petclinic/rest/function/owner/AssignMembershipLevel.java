package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.util.StringUtils;

/**
 * Assigns a newly built owner its numeric {@code membershipLevel} from the loyalty rules in
 * {@link MembershipLevel}. A newly created owner has no tenure yet, so the tenure factor never
 * applies here and the owner never exceeds level 3. Runs after {@link CountNamesakes} so the
 * namesake count is set.
 */
public class AssignMembershipLevel {

    /** A newly created owner has not yet accrued any tenure. */
    private static final long TENURE_AT_CREATION = 0;

    public void service(@Val Owner owner) {
        owner.setMembershipLevel(MembershipLevel.forOwner(
            StringUtils.hasText(owner.getEmail()), owner.getNamesakeCount(), TENURE_AT_CREATION));
    }
}

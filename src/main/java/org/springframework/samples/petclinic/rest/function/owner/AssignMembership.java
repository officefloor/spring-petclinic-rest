package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Membership;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.util.StringUtils;

/**
 * Scores a newly built owner: assigns its {@code membershipPoints} from the loyalty factors in
 * {@link Membership} and the {@code membershipLevel} those points map to. A newly created owner has
 * no tenure yet, so the tenure points never apply here and the level never exceeds 3. Runs after
 * {@link CountNamesakes} and {@link CountHouseholdMembers} so the namesake count and household size
 * are set.
 */
public class AssignMembership {

    /** A newly created owner has not yet accrued any tenure (zero elapsed fiscal years). */
    private static final long TENURE_AT_CREATION = 0;

    public void service(@Val Owner owner) {
        int points = Membership.points(StringUtils.hasText(owner.getEmail()), owner.getNamesakeCount(),
            owner.getHouseholdSize(), TENURE_AT_CREATION);
        owner.setMembershipPoints(points);
        owner.setMembershipLevel(Membership.levelFor(points));
    }
}

package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.function.common.Membership;

/**
 * Step of {@code POST /api/owners} that stamps the owner's numeric {@code membershipLevel},
 * applying the household ceiling: a new owner's level cannot exceed one above the current maximum
 * membershipLevel among their existing household members. With no existing household member no cap
 * applies and the owner keeps the level its own points earn.
 *
 * <p>The ceiling is evaluated over the household as it currently stands: each existing member sharing
 * this owner's {@code householdId} is re-scored with the current household size (this owner's
 * {@code householdMemberCount}, set by {@link AssignHouseholdSize}), so a member created when the
 * household was smaller still counts toward the ceiling at its current standing. The capped value is
 * stored on the owner and returned by the response mapper; {@code membershipPoints} is unaffected.
 *
 * <p>Runs after {@link AssignHouseholdSize} (which stamps {@code householdId} upstream via
 * {@link AssignHousehold} and this owner's {@code householdMemberCount}) and before {@link SaveOwner},
 * so the count reflects the existing members plus this owner and the level persists.
 */
public class AssignMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int naturalLevel = Membership.levelOf(owner);
        String householdId = owner.getHouseholdId();
        Integer householdSize = owner.getHouseholdMemberCount();

        int maxExistingLevel = 0;
        boolean hasExistingMember = false;
        if (householdId != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (householdId.equals(existing.getHouseholdId())) {
                    hasExistingMember = true;
                    maxExistingLevel = Math.max(maxExistingLevel,
                            Membership.levelOf(existing, householdSize));
                }
            }
        }

        // With no existing household member no cap applies.
        int level = hasExistingMember ? Math.min(naturalLevel, maxExistingLevel + 1) : naturalLevel;
        owner.setMembershipLevel(level);
    }
}

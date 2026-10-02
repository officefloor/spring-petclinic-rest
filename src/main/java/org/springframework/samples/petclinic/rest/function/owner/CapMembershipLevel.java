package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Caps the new owner's {@link Owner#getMembershipLevel() membershipLevel} at one above the current
 * maximum membershipLevel among their existing household members (see
 * {@link Household#membersOf(String, String, OwnerRepository)}). The cap is persisted on the owner (see
 * {@link Owner#getMembershipLevelCap()}) so the ceiling is honoured on later reads.
 *
 * <p>Runs before {@link SaveOwner}, so the owner being created is not yet among the household members and
 * does not cap itself. With no existing household member, no cap is applied.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        List<Owner> members = Household.membersOf(owner.getLastName(), owner.getPostcode(), ownerRepository);
        if (members.isEmpty()) {
            return; // no existing household member -> no cap
        }
        // Each member's level is read for the current household size (the members seen here, before this
        // owner is saved); read-only, so no member's transient state is mutated.
        int maxLevel = 0;
        for (Owner member : members) {
            maxLevel = Math.max(maxLevel, member.membershipLevelForHouseholdSize(members.size()));
        }
        owner.setMembershipLevelCap(maxLevel + 1);
    }
}

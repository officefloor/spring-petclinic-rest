package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.MembershipLevels;

/**
 * Captures the household level ceiling onto the freshly built {@link Owner} before it is saved.
 *
 * <p>A new owner's {@code membershipLevel} cannot exceed one above the current maximum membership
 * level among their existing household members — owners sharing the same non-blank
 * {@code householdId}. This step records that ceiling as {@link Owner#setMembershipLevelCap(Integer)}
 * ({@code max + 1}); the mapper later applies it via
 * {@link MembershipLevels#cappedLevelFor(Owner)}. With no existing household member the cap is left
 * unset, so no ceiling applies.
 *
 * <p>Runs after {@link AssignHousehold} (so the {@code householdId} is fixed) and before
 * {@link SaveOwner} (so the new owner — not yet persisted — is not counted among the existing
 * members).
 */
public class AssignMembershipLevelCap {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            return; // no household -> no ceiling
        }
        List<Owner> members = new ArrayList<>();
        for (Owner other : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(other.getId())) {
                continue; // never count self
            }
            if (Boolean.TRUE.equals(other.getDeleted())) {
                continue; // a soft-deleted owner is not a household member
            }
            if (householdId.equals(other.getHouseholdId())) {
                members.add(other);
            }
        }
        if (members.isEmpty()) {
            return; // no existing household member -> no ceiling
        }
        // Stamp each member's household size so its derived level is accurate.
        HouseholdMembers.stampAll(members, ownerRepository);
        int max = 0;
        for (Owner member : members) {
            max = Math.max(max, MembershipLevels.cappedLevelFor(member));
        }
        owner.setMembershipLevelCap(max + 1);
    }
}

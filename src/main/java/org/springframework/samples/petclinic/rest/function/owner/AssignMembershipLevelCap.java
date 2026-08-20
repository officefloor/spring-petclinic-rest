package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.MembershipLevels;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Applies the household level ceiling to a new owner: their membership level may not exceed one
 * above the current maximum membership level among their existing household members (owners sharing
 * this owner's {@code householdId}). The ceiling is stored as {@code membershipLevelCap} so it is
 * reflected wherever the level is derived (create response and later reads). When the owner has no
 * household or no existing household member, no cap applies and {@code membershipLevelCap} is left
 * null.
 *
 * <p>Runs on the create pipeline after {@link AssignHousehold} has assigned the deterministic
 * {@code householdId} and before {@link SaveOwner}, so the new owner is matched only against
 * existing owners.
 */
public class AssignMembershipLevelCap {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return;
        }
        List<Owner> members = ownerRepository.findAll().stream()
                .filter(existing -> householdId.equals(existing.getHouseholdId()))
                .filter(existing -> owner.getId() == null || !owner.getId().equals(existing.getId()))
                .toList();
        if (members.isEmpty()) {
            return;
        }
        // Once this owner is saved the household has one more member, so evaluate each existing
        // member's level with that household size — matching what a later read of the member reports
        // and keeping the ceiling consistent with the household-size membership points.
        int householdSize = members.size() + 1;
        int maxLevel = 0;
        for (Owner member : members) {
            member.setHouseholdMemberCount(householdSize);
            maxLevel = Math.max(maxLevel, MembershipLevels.of(member));
        }
        owner.setMembershipLevelCap(maxLevel + 1);
    }
}

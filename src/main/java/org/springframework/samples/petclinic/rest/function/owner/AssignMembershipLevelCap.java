package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs in the create-owner pipeline after {@link AssignHousehold} (which stamps the shared
 * {@code householdId}) and {@link AssignHouseholdSize} (which records the household's current size),
 * before {@link SaveOwner}. Stamps the owner's {@code membershipLevelCap}: a new owner's
 * {@link Owner#getMembershipLevel() membershipLevel} may not exceed one above the highest level
 * currently held by the other members of their household.
 *
 * <p>Every already-stored owner sharing this owner's non-null {@code householdId} is a household
 * member. Each is scored under the household as it stands once the new owner joins — that is, at the
 * current total {@link Owner#getHouseholdSize() householdSize} — so a member's household-size bonus
 * reflects the grown household rather than whatever it was when they themselves were created. The cap
 * is {@code max(memberLevel) + 1}. With no household ({@code householdId == null}) or no existing
 * member, no cap is stamped and the owner's level bands freely.
 */
public class AssignMembershipLevelCap {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return; // no shared household — nobody to be capped against
        }
        Integer size = owner.getHouseholdSize();
        int householdSize = size == null ? 1 : size;
        Integer maxLevel = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                int level = existing.membershipLevelForHouseholdSize(householdSize);
                if (maxLevel == null || level > maxLevel) {
                    maxLevel = level;
                }
            }
        }
        if (maxLevel != null) {
            owner.setMembershipLevelCap(maxLevel + 1);
        }
    }
}

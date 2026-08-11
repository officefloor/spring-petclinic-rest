package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Applies the household ceiling to an owner's membership level: a new owner's
 * {@code membershipLevel} cannot exceed one above the current maximum membership level among the
 * other members of their household (all owners sharing the same {@code householdId}). With no other
 * household member, no cap applies and the owner's own level is returned unchanged.
 *
 * <p>Each existing member's level is evaluated at the household's <em>current</em> size (the number
 * of members sharing the household now, including this owner), not the size stored on that member
 * when it was created — so a household that has since grown to three or more lifts every member's
 * household-size bonus, and with it the ceiling.
 */
public final class HouseholdLevelCap {

    private HouseholdLevelCap() {
    }

    /**
     * The owner's membership level capped at one above the maximum level among their other household
     * members, or their own level when the household has no other member.
     */
    public static int cappedMembershipLevel(Owner owner, OwnerMapper ownerMapper,
            OwnerRepository ownerRepository) {
        int ownLevel = ownerMapper.membershipLevel(owner);
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return ownLevel;
        }
        List<Owner> members = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())
                    && (owner.getId() == null || !owner.getId().equals(existing.getId()))) {
                members.add(existing);
            }
        }
        if (members.isEmpty()) {
            return ownLevel;
        }
        int currentHouseholdSize = members.size() + 1; // the other members plus this owner
        int maxMemberLevel = 0;
        for (Owner member : members) {
            maxMemberLevel = Math.max(maxMemberLevel,
                    ownerMapper.membershipLevelForHousehold(member, currentHouseholdSize));
        }
        return Math.min(ownLevel, maxMemberLevel + 1);
    }
}

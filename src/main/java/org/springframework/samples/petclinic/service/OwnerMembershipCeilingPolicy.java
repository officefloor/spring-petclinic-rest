package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Business rule: a new owner's {@code membershipLevel} cannot exceed one above the current maximum
 * {@code membershipLevel} among the other members of their household (owners sharing the same
 * {@code householdId}); the capped level is returned. With no existing household member no cap
 * applies. Kept as a small, self-contained unit so the ceiling can be applied from the read flow
 * without adding complexity to the mapper, controller, or service.
 */
@Component
public class OwnerMembershipCeilingPolicy {

    private static ClinicService clinicService;

    OwnerMembershipCeilingPolicy(ClinicService clinicService) {
        OwnerMembershipCeilingPolicy.clinicService = clinicService;
    }

    /**
     * Derive the owner's {@code membershipLevel}, capped at one above the highest level among their
     * existing household members.
     *
     * @param owner the owner whose capped membership level to derive
     * @return the capped level
     */
    public static int cappedMembershipLevel(Owner owner) {
        int level = OwnerMembershipLevelPolicy.membershipLevel(owner);
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return level;
        }
        Integer maxOther = null;
        for (Owner existing : clinicService.findAllOwners()) {
            if (householdId.equals(existing.getHouseholdId())
                && !existing.getId().equals(owner.getId())) {
                int other = OwnerMembershipLevelPolicy.membershipLevel(existing);
                maxOther = (maxOther == null || other > maxOther) ? other : maxOther;
            }
        }
        return maxOther == null ? level : Math.min(level, maxOther + 1);
    }
}

package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Caps a new owner's {@code membershipLevel} at one above the highest level among the
 * household members that predate it (same {@code householdId}, lower id). With no earlier
 * household member, no cap applies and the level is returned unchanged.
 */
final class HouseholdMembershipCap {

    private HouseholdMembershipCap() {
    }

    static Integer apply(Owner owner, Integer level, OwnerRepository ownerRepository, OwnerMapper ownerMapper) {
        if (level == null || owner.getId() == null || owner.getHouseholdId() == null) {
            return level;
        }
        return ownerRepository.findAll().stream()
                .filter(existing -> existing.getId() != null && existing.getId() < owner.getId()
                        && owner.getHouseholdId().equals(existing.getHouseholdId()))
                .map(ownerMapper::membershipLevel)
                .max(Integer::compare)
                .map(max -> Math.min(level, max + 1))
                .orElse(level);
    }
}

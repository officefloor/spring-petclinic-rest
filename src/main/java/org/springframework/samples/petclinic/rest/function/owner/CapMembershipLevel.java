package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Caps the new owner's membershipLevel at one above the highest membershipLevel among the existing
 * members of their household (same computed {@code householdId}, see {@link HouseholdId}). With no
 * existing household member the natural level stands, i.e. no cap applies.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository, OwnerMapper ownerMapper) {
        String household = HouseholdId.of(owner.getLastName(), owner.getPostcode());
        int highest = ownerRepository.findAll().stream()
                .filter(other -> household.equals(HouseholdId.of(other.getLastName(), other.getPostcode())))
                .mapToInt(ownerMapper::membershipLevel)
                .max().orElse(-1);
        if (highest < 0) {
            return;
        }
        int natural = MembershipPoints.level(MembershipPoints.of(owner));
        owner.setMembershipLevel(Math.min(natural, highest + 1));
    }
}

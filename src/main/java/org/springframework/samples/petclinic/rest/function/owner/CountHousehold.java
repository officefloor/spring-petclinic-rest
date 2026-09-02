package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many owners belong to this owner's household (same computed {@code householdId} —
 * last name and postcode, see {@link HouseholdId}) after this create, i.e. the existing members
 * plus the new owner. Drives the 'GOLD' membership tier.
 */
public class CountHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String household = HouseholdId.of(owner.getLastName(), owner.getPostcode());
        long existing = ownerRepository.findAll().stream()
                .filter(other -> household.equals(HouseholdId.of(other.getLastName(), other.getPostcode())))
                .count();
        owner.setHouseholdCount((int) existing + 1);
    }
}

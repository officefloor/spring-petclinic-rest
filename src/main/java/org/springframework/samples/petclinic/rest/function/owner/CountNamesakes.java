package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records this owner's household size at create time: how many existing owners resolve to
 * the same {@link HouseholdId} (lastName and postcode), before the new owner is saved. This
 * count feeds the membership level, so a new/sole household (count 0) earns the extra tier.
 */
public class CountNamesakes {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = HouseholdId.of(owner.getLastName(), owner.getPostcode());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(HouseholdId.of(existing.getLastName(), existing.getPostcode()))) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }
}

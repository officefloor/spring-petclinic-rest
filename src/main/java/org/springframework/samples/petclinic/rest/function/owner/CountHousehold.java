package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many owners the new owner's household will hold once this create completes — the new
 * owner plus every existing owner sharing the same household (same last name and address, compared
 * via {@link HouseholdNormalizer}). Existing owners can only share that identity when they too opted
 * into a shared household ({@link EnsureUniqueHousehold} rejects any un-acknowledged collision), so a
 * matching owner is always a genuine household member. Runs before {@link SaveOwner}, so the new
 * owner is not yet persisted and is counted explicitly. Mutates the built {@link Owner} in place.
 */
public class CountHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String key = HouseholdNormalizer.key(owner.getLastName(), owner.getAddress());
        int members = 1;
        for (Owner existing : ownerRepository.findAll()) {
            if (key.equals(HouseholdNormalizer.key(existing.getLastName(), existing.getAddress()))) {
                members++;
            }
        }
        owner.setHouseholdSize(members);
    }
}

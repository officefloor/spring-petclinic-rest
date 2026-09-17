package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many owners the new owner's household will hold once this create completes — the new
 * owner plus every existing owner sharing the same {@link HouseholdNormalizer#id(String, String)
 * household id} (same last name and postcode). Runs before {@link SaveOwner}, so the new owner is not
 * yet persisted and is counted explicitly. Mutates the built {@link Owner} in place.
 */
public class CountHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        int members = 1;
        for (Owner existing : ownerRepository.findAll()) {
            if (HouseholdNormalizer.belongsTo(existing, householdId)) {
                members++;
            }
        }
        owner.setHouseholdSize(members);
    }
}

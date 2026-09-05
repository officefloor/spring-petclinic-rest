package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Sets {@code householdSize} to the number of members the owner's household has after this create:
 * every existing owner sharing the same household plus the owner being created. The household is now
 * the deterministic {@link HouseholdId} derived from the last name and postcode, so two owners belong
 * to the same household exactly when they share a {@code householdId} — the same rule duplicate
 * detection ({@link RejectDuplicateOwner}) uses. Runs before the owner is saved, so
 * {@link OwnerRepository#findAll()} sees only the owners that existed before this create, which are then
 * counted alongside this one.
 */
public class CountHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = HouseholdId.of(owner.getLastName(), owner.getPostcode());
        int count = 1;
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(HouseholdId.of(existing.getLastName(), existing.getPostcode()))) {
                count++;
            }
        }
        owner.setHouseholdSize(count);
    }
}

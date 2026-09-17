package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many owners the new owner's household holds once this owner is added: the
 * existing owners that share this owner's deterministic {@code householdId} (same last name and
 * postcode, see {@link OwnerHouseholds}) plus the new owner itself. A household with no other
 * members has a size of 1.
 *
 * <p>Runs after {@link AssignOwnerHousehold} — which assigns the {@code householdId} this counts
 * on — and before {@link SaveOwner}, so the count reflects the household membership at creation
 * time and never counts the new owner twice.
 */
public class AssignOwnerHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int count = 1; // the new owner, not yet saved
        String householdId = owner.getHouseholdId();
        if (householdId != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (householdId.equals(existing.getHouseholdId())) {
                    count++;
                }
            }
        }
        owner.setHouseholdSize(count);
    }
}

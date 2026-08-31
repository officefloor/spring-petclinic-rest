package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * On create, records how many owners share this owner's household (matched by
 * {@code householdId}) once this one joins, as {@code householdSize}. Runs after the household id
 * is assigned and before {@code SaveOwner}, so the not-yet-persisted owner is counted via the
 * seeded count of one.
 */
public class AssignOwnerHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int count = 1;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getHouseholdId() != null
                    && owner.getHouseholdId().equals(existing.getHouseholdId())) {
                count++;
            }
        }
        owner.setHouseholdSize(count);
    }
}

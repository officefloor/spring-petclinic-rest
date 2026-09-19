package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many owners belong to the new owner's household — those sharing its
 * {@code householdId} — once this create completes. Runs after {@link AssignHouseholdId} has set
 * the id and before {@link SaveOwner} persists the entity, so {@code findAll()} sees only the
 * owners that existed before this create; the new owner is counted too by starting from one. The
 * size is stored on the owner for later reporting.
 */
public class CountHouseholdMembers {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        int count = 1;
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        owner.setHouseholdSize(count);
    }
}

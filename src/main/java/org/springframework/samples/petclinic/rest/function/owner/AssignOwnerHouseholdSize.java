package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many owners share the built owner's household — the owners with the same
 * {@code householdId} — counting this owner as well. Runs after {@link AssignOwnerHousehold}
 * has assigned the shared id but before the owner is saved, so the count reflects the
 * household size after this create, and mutates the built {@link Owner} in place. An owner
 * with no household id stands alone, so its size is 1.
 */
public class AssignOwnerHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int size = 1;
        String householdId = owner.getHouseholdId();
        if (householdId != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (householdId.equals(existing.getHouseholdId())) {
                    size++;
                }
            }
        }
        owner.setHouseholdSize(size);
    }
}

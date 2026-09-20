package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many owners share the new owner's household. Counts the existing owners with
 * the same {@code householdId} (assigned earlier by {@link AssignHousehold} from last name and
 * postcode) and adds one for the owner being created, so the count reflects the household size
 * that results from this create. An owner whose last name and postcode match no one else is a
 * household of one.
 */
public class CountHouseholdMembers {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int count = 1;
        for (Owner existing : ownerRepository.findAllActive()) {
            if (Households.sameHousehold(owner, existing)) {
                count++;
            }
        }
        owner.setHouseholdMemberCount(count);
    }
}

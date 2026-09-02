package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Counts the members of an owner's household: the owners sharing this owner's {@link HouseholdId}
 * (last name and postcode). The owner itself is included, so a freshly created owner counts towards
 * its own household total.
 */
public final class HouseholdSize {

    private HouseholdSize() {
    }

    public static int of(Owner owner, OwnerRepository repository) {
        String householdId = HouseholdId.of(owner);
        int count = 0;
        for (Owner other : repository.findAll()) {
            if (householdId.equals(HouseholdId.of(other))) {
                count++;
            }
        }
        return count;
    }
}

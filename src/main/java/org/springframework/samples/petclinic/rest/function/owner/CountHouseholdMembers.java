package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Computes this owner's {@code householdSize} — the number of owners that belong to the same household,
 * i.e. that share the same deterministic {@link Household#id(String, String) household id} (same last
 * name and postcode), including this owner. Used by {@link Owner#getMembershipPoints()} to award points
 * for a household of three or more. Computed per request and mutated onto the owner in place, not
 * persisted, because household membership changes as owners are created and removed; it is recomputed
 * identically on each read.
 */
public class CountHouseholdMembers {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = Household.id(owner.getLastName(), owner.getPostcode());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(Household.id(existing.getLastName(), existing.getPostcode()))) {
                count++;
            }
        }
        owner.setHouseholdSize(count);
    }
}

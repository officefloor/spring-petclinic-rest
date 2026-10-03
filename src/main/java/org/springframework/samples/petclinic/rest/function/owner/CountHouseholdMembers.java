package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code householdSize}: the number of owners that belong to the same
 * household — {@code (lastName, postcode)}, keyed by {@link Households} — counting the
 * existing owners plus the owner being created. Runs after {@link AssignHousehold} has
 * stamped the {@code householdId}, so it reflects that same household definition, and
 * before the owner is saved, so existing members are counted once and the new owner is
 * added as the {@code + 1}.
 *
 * <p>An owner with no postcode belongs to no household (null {@code householdId}); it is
 * recorded as a household of one.
 */
public class CountHouseholdMembers {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            owner.setHouseholdSize(1);
            return;
        }
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() == null) {
                continue;
            }
            if (householdId.equals(Households.householdId(existing.getLastName(), existing.getPostcode()))) {
                count++;
            }
        }
        owner.setHouseholdSize(count + 1);
    }
}

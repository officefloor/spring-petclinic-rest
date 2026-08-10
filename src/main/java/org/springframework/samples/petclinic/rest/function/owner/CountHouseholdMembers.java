package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs after {@link AssignOwnerHousehold} (which sets the deterministic {@code householdId}) and before
 * the owner is saved. Sets the owner's {@code householdSize} to the number of members its household has
 * after this create: the existing owners already sharing the same {@code householdId} plus this new one
 * (not yet persisted). The household is keyed on (lastName, postcode), so each existing owner's household
 * is derived the same deterministic way rather than read from a stored value. The size is mutated in
 * place via {@code @Val} so the save/respond steps persist and return it.
 */
public class CountHouseholdMembers {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        int size = 1; // this owner, not yet persisted
        if (householdId != null) {
            size += (int) ownerRepository.findAll().stream()
                    .filter(existing -> householdId.equals(
                            OwnerIdentity.householdId(existing.getLastName(), existing.getPostcode())))
                    .count();
        }
        owner.setHouseholdSize(size);
    }
}

package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns a shared {@code householdId} when a create-owner request opts into a shared household
 * with {@code sharesHousehold} true and there is already an owner in the same household (same last
 * name and address, see {@link Household}). The identifier is stable: derived deterministically
 * from the normalized last name and address, so every owner in the same household receives the same
 * value. It is also written back onto the existing same-household owners so both sides of the join
 * carry the identifier. Runs after {@link BuildOwner} has produced the {@link Owner} and before
 * {@link SaveOwner} persists it, so the new owner is saved with the id.
 *
 * <p>When {@code sharesHousehold} is not set, or there is no existing same-household owner, no
 * identifier is assigned and the owner is saved without one.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        Household.assign(owner, request, ownerRepository);
    }
}

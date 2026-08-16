package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns a shared {@code householdId} when the request opts in with {@code sharesHousehold: true}.
 *
 * <p>The identifier is derived deterministically from the normalized last name and address (see
 * {@link OwnerIdentity#householdIdFor}), so it is stable: every owner living at the same address
 * under the same last name gets the same value regardless of creation order. The new owner and every
 * existing owner in that household are stamped with it, so both sides of an intentionally shared
 * household report the identifier. When {@code sharesHousehold} is not true no identifier is assigned,
 * and the owner's identity key (see {@link OwnerIdentity}) uses an empty household component.
 *
 * <p>Runs after {@link BuildOwner} (which publishes the new owner) and before {@link SaveOwner}.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String householdId = OwnerIdentity.householdIdFor(owner.getLastName(), owner.getAddress());
        owner.setHouseholdId(householdId);
        // Stamp existing members of the same household so both sides share the identifier.
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(OwnerIdentity.householdIdFor(existing.getLastName(), existing.getAddress()))
                    && !householdId.equals(existing.getHouseholdId())) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }
}

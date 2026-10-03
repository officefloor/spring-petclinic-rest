package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * When the request sets {@code sharesHousehold} true, joins the new owner to the
 * household formed by owners that share the same last name and address, using the
 * shared comparison in {@link Households} so the {@code householdId} it assigns matches
 * the one {@link EnsureUniqueIdentity} folds into the identity key.
 *
 * <p>Every owner in a household carries the same {@code householdId} — a stable
 * identifier derived from the normalized last name and address, so all members
 * compute the same value independently. The new owner receives it, and any existing
 * household members that do not yet carry it are back-filled so the whole household
 * shares one identifier.
 *
 * <p>Does nothing when {@code sharesHousehold} is absent or false, leaving a solo
 * owner's {@code householdId} null.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = Households.normalize(owner.getLastName());
        String address = Households.normalizeAddress(owner.getAddress());
        String householdId = Households.deriveHouseholdId(lastName, address);
        owner.setHouseholdId(householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (Households.matches(existing, lastName, address)
                    && !householdId.equals(existing.getHouseholdId())) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }
}

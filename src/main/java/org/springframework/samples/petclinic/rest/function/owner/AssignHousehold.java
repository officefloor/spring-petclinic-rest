package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * When the request opts into sharing a household ({@code sharesHousehold} true), assigns a
 * stable {@code householdId} derived from the normalized last name and address, so every
 * owner living together at the same address shares one identifier. Existing owners of the
 * same household that do not yet carry an id are back-filled with the same value, so both
 * the joining owner and the owner already at that address end up sharing it. Derivation and
 * normalization are centralised in {@link Households}, which {@link IdentityKeys} also uses
 * so an owner's stored {@code householdId} matches the household component of its identity key.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner,
            OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = Households.normalize(owner.getLastName());
        String address = AddressNormalizer.normalize(owner.getAddress());
        String householdId = Households.idFor(owner.getLastName(), owner.getAddress());
        owner.setHouseholdId(householdId);
        // Back-fill existing household members so both sides share the identifier.
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getHouseholdId() == null
                    && lastName.equals(Households.normalize(existing.getLastName()))
                    && address.equals(AddressNormalizer.normalize(existing.getAddress()))) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }
}

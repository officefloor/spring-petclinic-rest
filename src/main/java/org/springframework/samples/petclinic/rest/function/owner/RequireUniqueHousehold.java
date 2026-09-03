package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerHouseholdException;

/**
 * Rejects a create request that duplicates another owner in the same household, i.e. shares the
 * computed {@code householdId} (last name and postcode, see {@link HouseholdId}) <em>and</em> the
 * same normalized address. Owners at different addresses in the same household coexist as household
 * members. A request with {@code sharesHousehold} true opts out. Requests without a postcode are
 * not grouped into a household. A match rejects 409.
 */
public class RequireUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())
                || request.getPostcode() == null || request.getPostcode().isBlank()) {
            return;
        }
        String household = HouseholdId.of(request.getLastName(), request.getPostcode());
        String address = AddressNormalizer.normalize(request.getAddress());
        for (Owner owner : ownerRepository.findAll()) {
            if (owner.isDeleted()) {
                continue;
            }
            if (household.equals(HouseholdId.of(owner.getLastName(), owner.getPostcode()))
                    && address.equals(AddressNormalizer.normalize(owner.getAddress()))) {
                throw new DuplicateOwnerHouseholdException(owner.getLastName(), owner.getAddress());
            }
        }
    }
}

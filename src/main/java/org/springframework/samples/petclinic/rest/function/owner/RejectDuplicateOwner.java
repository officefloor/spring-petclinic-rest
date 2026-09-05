package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Household duplicate detection for create-owner — "the duplicate block". Duplicate detection keys off
 * the household, which is now the deterministic {@link HouseholdId} derived from the last name and
 * postcode: two owners belong to the same household exactly when they share a {@code householdId}.
 * Because the household is keyed on {@code (lastName, postcode)}, a second owner matching an existing
 * owner's last name and postcode is a household duplicate and is rejected via
 * {@link DuplicateIdentityException}, which the global handler turns into a 409.
 *
 * <p>A request that sets {@code sharesHousehold} true bypasses this block: it is a declared household
 * member and is created (see {@link DetectPossibleDuplicate}, which does not flag a declared member as a
 * suspected duplicate). {@code sharesHousehold} now only bypasses the duplicate block; the shared
 * {@code householdId} is derived automatically, not linked. Runs before the owner is built and saved,
 * so {@link OwnerRepository#findAll()} sees only the owners that existed before this create.
 */
public class RejectDuplicateOwner {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member — bypass the duplicate block
        }
        String householdId = HouseholdId.of(request.getLastName(), request.getPostcode());
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // soft-deleted owners are ignored by the duplicate check
            }
            if (householdId.equals(HouseholdId.of(existing.getLastName(), existing.getPostcode()))) {
                throw new DuplicateIdentityException(IdentityKey.of(request.getTelephone(),
                        request.getEmail(), request.getLastName(), request.getPostcode()));
            }
        }
    }
}

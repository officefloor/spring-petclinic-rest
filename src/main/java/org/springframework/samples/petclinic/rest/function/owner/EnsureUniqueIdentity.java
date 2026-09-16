package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The duplicate block: rejects a create whose {@code (lastName, postcode)} household —
 * identified by its deterministic {@code householdId} (see {@link Households#householdId})
 * — is already occupied by an existing owner, as a 409 via
 * {@link DuplicateIdentityException}. Because the household is keyed on last name and
 * postcode, a second owner at the same one is a household duplicate.
 *
 * <p>A request that opts in with {@code sharesHousehold=true} bypasses the block: it is a
 * declared member of that household and is created (see {@link AssignPossibleDuplicate},
 * which also leaves a declared member unflagged). A request with no postcode has no
 * household key and cannot collide.
 *
 * <p>Runs before {@link BuildOwner}, so a collision is caught before any owner is built or
 * saved.
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String householdId = Households.householdId(request.getLastName(), request.getPostcode());
        if (householdId == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                throw new DuplicateIdentityException(householdId);
            }
        }
    }
}

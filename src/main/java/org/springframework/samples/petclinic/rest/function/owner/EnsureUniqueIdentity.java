package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Duplicate detection for {@code POST /api/owners}, keyed on the deterministic {@code householdId}
 * (see {@link AssignHousehold}). Rejects a create with 409 via {@link DuplicateIdentityException} in
 * either of two cases:
 *
 * <ul>
 * <li><b>Household duplicate</b> — an existing owner already has the same {@code householdId} (i.e.
 * the same last name and postcode, so it is the same household). This is bypassed when the request
 * sets {@code sharesHousehold}, in which case the new owner is created as a declared household
 * member.</li>
 * <li><b>Exact identity</b> — the new owner's WHOLE {@code identityKey} (see {@link IdentityKey})
 * equals an existing owner's, i.e. the same telephone, email and household. A truly identical record
 * is always a conflict, even for a declared member.</li>
 * </ul>
 *
 * <p>Runs after {@link AssignHousehold}, so the new owner's householdId is finalized before it is
 * compared, and before {@link SaveOwner}.
 */
public class EnsureUniqueIdentity {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        boolean sharesHousehold = Boolean.TRUE.equals(request.getSharesHousehold());
        String identityKey = IdentityKey.of(owner);
        String householdId = owner.getHouseholdId();
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // the owner being created is not yet its own duplicate
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner no longer blocks a create
            }
            if (!sharesHousehold && householdId != null
                    && householdId.equals(existing.getHouseholdId())) {
                throw new DuplicateIdentityException(householdId);
            }
            if (IdentityKey.of(existing).equals(identityKey)) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}

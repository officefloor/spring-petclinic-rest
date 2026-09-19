package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The duplicate block: rejects a create-owner request that repeats an existing live owner's
 * {@link Owner#getIdentityKey() identity key} (same normalized telephone, email and
 * phonetically-equal last name), responding 409. Soft-deleted owners are ignored, so a deleted
 * owner no longer blocks a new one. Two owners that merely share a last name and postcode but
 * differ in telephone have different identity keys and are not blocked here: they are created and
 * flagged by {@link AssignOwnerPossibleDuplicate}. A request opting in with
 * {@code sharesHousehold} declares the new owner a genuine member and bypasses the block. Runs
 * after {@link BuildOwner} (so the entity's telephone, email and name exist) and after
 * {@link ValidateOwnerEmailDomain} (so the email-domain blocklist applies first), and before
 * {@link SaveOwner} persists a duplicate.
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        if (!Identities.duplicatesOf(ownerRepository, owner).isEmpty()) {
            throw new DuplicateIdentityException(owner.getIdentityKey());
        }
    }
}

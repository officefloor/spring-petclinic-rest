package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * The duplicate check for creating owners. A household — {@code (normalizedLastName, postcode)}
 * expressed as a deterministic {@code householdId} (see {@link OwnerIdentityKey#householdId(String,
 * String)}) — may legitimately have several members (their membership levels are capped relative to
 * each other by {@link CapMembershipLevel}), so sharing a household is no longer a duplicate on its own.
 * Only a <em>true</em> duplicate is rejected with 409 via {@link DuplicateOwnerException}: an existing
 * non-deleted owner in the same household with the same (E.164) telephone — i.e. the same person.
 *
 * <p>Setting {@code sharesHousehold} true bypasses this block: the owner is then created as a declared
 * household member (and, being declared, is not flagged as a possible duplicate by
 * {@link AssignPossibleDuplicate}). Runs before {@link BuildOwner}.
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member — bypass the household duplicate block
        }
        String householdId = OwnerIdentityKey.householdIdOf(request);
        String telephone = E164Telephone.toE164OrNull(request.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer blocks a duplicate
            }
            if (householdId.equals(OwnerIdentityKey.householdIdOf(existing))
                    && telephone != null
                    && telephone.equals(E164Telephone.toE164OrNull(existing.getTelephone()))) {
                throw new DuplicateOwnerException(householdId);
            }
        }
    }
}

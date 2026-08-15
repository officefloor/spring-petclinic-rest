package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerIdentityException;

/**
 * The duplicate-detection step of {@code POST /api/owners}. Duplicate detection is the single
 * {@code identityKey} (see {@link OwnerIdentityKey}): a request is a duplicate only when an existing
 * owner carries the <em>same whole key</em> — normalized telephone, email and {@code soundex(lastName)}
 * all equal. There is no separate household-duplicate block: the postcode is not part of the key, so
 * two owners with the same last name and postcode but different telephones have different keys and are
 * both allowed (the weaker overlap is a soft match, see {@link AssignPossibleDuplicate}); only a
 * genuine re-submission of the same owner is rejected with 409.
 *
 * <p>The email-domain blocklist is applied earlier, in {@link ValidateNewOwner}, so a disposable
 * domain is a 400 before this step runs. Soft-deleted owners are ignored here.
 *
 * <p>The {@code sharesHousehold} hint bypasses this block: a request that declares itself a member
 * of a shared household is allowed through and created as a legitimate second member (and is not
 * flagged as a possible duplicate, see {@link AssignPossibleDuplicate}).
 *
 * <p>Runs after {@link ValidateNewOwner} has normalized the request and before {@link BuildOwner},
 * so a duplicate is a 409 rather than a persisted record.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerIdentityException {
        if (Household.sharesHousehold(request)) {
            return; // declared household member: bypass the duplicate block
        }
        String identityKey = OwnerIdentityKey.forRequest(request);
        for (Owner owner : ownerRepository.findAll()) {
            if (owner.isDeleted()) {
                continue; // a soft-deleted owner no longer blocks a new create
            }
            if (identityKey.equals(OwnerIdentityKey.forOwner(owner))) {
                throw new DuplicateOwnerIdentityException(Household.idFor(request));
            }
        }
    }
}

package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The duplicate block: a request is a hard duplicate when an existing active owner has the same
 * {@link IdentityKey identity key} (SHA-256 over the canonical telephone, email and {@link
 * org.springframework.samples.petclinic.util.Soundex Soundex} of the last name), and is rejected with
 * a 409 via {@link DuplicateIdentityException}. Because the telephone is part of the key, two owners
 * with the same last name and postcode but a <em>different</em> telephone are not a hard duplicate:
 * the request passes this block and is instead marked for follow-up by {@link FlagPossibleDuplicate}
 * and held to its household level ceiling by {@link CapMembershipLevel}. Setting {@code
 * sharesHousehold} bypasses this block outright, so the new owner is created as a declared household
 * member. The email-domain blocklist ({@link ValidateEmailDomain}) is applied earlier in the
 * pipeline, so a blocked address is already rejected before this step. Soft-deleted owners are
 * ignored, so a match that has since been deleted no longer blocks the create.
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String identityKey = IdentityKey.forOwner(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isActive() && identityKey.equals(IdentityKey.forOwner(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}

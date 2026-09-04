package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * The duplicate check for creating owners, keyed on the single {@link OwnerIdentityKey} —
 * SHA-256 over {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}. A create is
 * rejected with 409 via {@link DuplicateOwnerException} when its identity key equals that of an
 * existing non-deleted owner: the same person (same telephone, email and sound-alike last name).
 *
 * <p>Because the telephone is part of the key, two owners with the same last name and postcode but
 * different telephones are no longer a hard household duplicate here — they are created and flagged a
 * soft match by {@link AssignPossibleDuplicate} instead. The email-domain blocklist runs earlier in the
 * pipeline ({@link ValidateOwnerEmailDomain}), so a blocked email is a 400 before this check. Setting
 * {@code sharesHousehold} true bypasses this block (and, being declared, the owner is not flagged as a
 * possible duplicate). Runs before {@link BuildOwner}.
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member — bypass the duplicate block
        }
        String identityKey = OwnerIdentityKey.of(request);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer blocks a duplicate
            }
            if (identityKey.equals(OwnerIdentityKey.of(existing))) {
                throw new DuplicateOwnerException(identityKey);
            }
        }
    }
}

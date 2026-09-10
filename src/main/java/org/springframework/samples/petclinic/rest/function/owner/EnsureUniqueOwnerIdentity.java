package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request whose {@link OwnerIdentityKey} equals that of an existing
 * (non-deleted) owner, responding 409. The identityKey is the SHA-256 hex over the normalized
 * telephone, lower-cased email and Soundex last name, so this is the single duplicate-detection
 * check: there is no longer a separate household-duplicate block.
 *
 * <p>Because the telephone is part of the key, two owners with the same last name and postcode but
 * different telephones have different keys and are both allowed (the second is instead flagged as a
 * possible duplicate — see {@link AssignOwnerPossibleDuplicate}).
 *
 * <p>Runs after {@link ValidateOwnerFields}, so the email-domain blocklist and telephone
 * normalization have already been applied to the request. A soft-deleted owner no longer blocks a
 * new one.
 */
public class EnsureUniqueOwnerIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerIdentityException {
        String identityKey = OwnerIdentityKey.of(
                request.getTelephone(), request.getEmail(), request.getLastName());
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer blocks a new one
            }
            if (identityKey.equals(OwnerIdentityKey.of(existing))) {
                throw new DuplicateOwnerIdentityException(identityKey);
            }
        }
    }
}

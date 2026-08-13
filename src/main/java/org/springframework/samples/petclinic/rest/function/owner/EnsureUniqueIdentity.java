package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Consolidated duplicate detection for create-owner: rejects a request whose whole derived
 * {@code identityKey} equals that of an existing owner, responding 409 via
 * {@link DuplicateIdentityException}.
 *
 * <p>The key (see {@link Owner#getIdentityKey()}) is the SHA-256 hex digest over
 * {@code normalizedTelephone|lowerEmail|soundex(lastName)}, so the former separate telephone,
 * email and household checks are now a single full-key comparison. Because the telephone is part
 * of the key, two owners with the same last name and postcode but different telephones have
 * different identity keys and are both allowed (the second is a soft match); only an exact
 * full-key match is a duplicate. The email-domain blocklist is applied earlier, during field
 * validation, so a blocked domain is a 400 before this check runs.
 *
 * <p>Runs after {@link AssignHousehold} and before {@link SaveOwner} persists anything. The new
 * owner is not yet in the repository, so it is never compared against itself; soft-deleted owners
 * are ignored.
 */
public class EnsureUniqueIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = owner.getIdentityKey();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer blocks a duplicate
            }
            if (identityKey.equals(existing.getIdentityKey())) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}

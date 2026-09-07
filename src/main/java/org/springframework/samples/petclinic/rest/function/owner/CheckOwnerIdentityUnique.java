package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The single duplicate check for create-owner, keyed on the whole {@link OwnerIdentity#key(Owner)
 * identity key} (SHA-256 hex over normalized telephone, lower-cased email and the Soundex of the last
 * name). Because the telephone and email are part of the key, two owners who share a last name (and
 * even a postcode) but differ in telephone or email have different keys and are BOTH allowed
 * (surfaced instead as a soft match by {@link CheckOwnerPossibleDuplicate}); only an owner whose
 * whole identity key equals an existing (non-deleted) owner's is rejected with a 409 Conflict.
 * {@code sharesHousehold} true still bypasses the check entirely. The email-domain blocklist
 * ({@link CheckOwnerEmailDomain}) runs earlier in the pipeline, so a blocked address is a 400 before
 * this check runs. This is now the ONLY duplicate rejection: the household id no longer produces its
 * own 409.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return; // opted in as a declared household member: bypass the duplicate block
        }
        String identityKey = OwnerIdentity.key(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // same record (e.g. re-save), not a conflict
            }
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer blocks a new one
            }
            if (identityKey.equals(OwnerIdentity.key(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}

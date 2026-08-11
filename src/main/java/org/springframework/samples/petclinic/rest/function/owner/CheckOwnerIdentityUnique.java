package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerIdentityConflictException;

/**
 * The single, consolidated duplicate check for {@code POST /api/owners}: rejects the request with
 * 409 when the new owner's whole {@link OwnerIdentityKey identityKey} (the 64-hex SHA-256 over
 * {@code normalizedTelephone|lowerEmail|soundex(lastName)}) equals an existing owner's. This is now
 * the only duplicate rule — the former separate telephone, email and household-duplicate checks are
 * gone. Because the telephone is part of the key, two people with the same last name (same soundex)
 * and postcode but different telephones have different keys and are both allowed — only an exact
 * full-key match is a duplicate; the near miss is a soft match (see {@link AssignPossibleDuplicate}).
 *
 * <p>Soft-deleted owners are ignored, so a new identity is never blocked by one. Runs before
 * {@link SaveOwner} so the not-yet-saved owner is not compared against itself. The email-domain
 * blocklist has already been applied upstream (in {@link ValidateOwnerFields}).
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerIdentityConflictException {
        String identityKey = OwnerIdentityKey.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue;
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner no longer blocks a new identity
            }
            if (identityKey.equals(OwnerIdentityKey.of(existing))) {
                throw new OwnerIdentityConflictException(
                        "An owner with identity " + identityKey + " already exists");
            }
        }
    }
}

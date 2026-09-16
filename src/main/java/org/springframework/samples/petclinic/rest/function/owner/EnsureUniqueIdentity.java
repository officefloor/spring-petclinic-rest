package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.util.IdentityKey;

/**
 * The duplicate block: rejects a create that re-registers the same identity as an existing
 * owner, as a 409 via {@link DuplicateIdentityException}. An owner's identity is its
 * {@link IdentityKey} — the SHA-256 digest over normalized telephone, lower-cased email and
 * the Soundex of the last name — and a request whose key matches a non-deleted owner's is
 * the same person registering twice.
 *
 * <p>Because the telephone is part of the key, two owners sharing a last name and postcode
 * but with <em>different</em> telephones are distinct identities: the second is allowed
 * through, created and flagged as a soft duplicate (see {@link AssignPossibleDuplicate}). A
 * request that opts in with {@code sharesHousehold=true} is a declared household member and
 * bypasses the block (and is left unflagged).
 *
 * <p>Runs after the telephone and email are normalized (matching the stored form) and after
 * the disposable-email blocklist, and before {@link BuildOwner}, so a collision is caught
 * before any owner is built or saved.
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String identityKey = IdentityKey.of(request.getTelephone(), request.getEmail(), request.getLastName());
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner no longer holds its identity
            }
            if (identityKey.equals(IdentityKey.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}

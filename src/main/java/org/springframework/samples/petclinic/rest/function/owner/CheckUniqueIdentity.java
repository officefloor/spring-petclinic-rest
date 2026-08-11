package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.util.OwnerIdentities;

/**
 * Rejects a create-owner request that exactly duplicates an existing owner's identity. The identity
 * is the {@code identityKey} — the SHA-256 of {@code telephone + email + soundex(lastName)} —
 * computed by {@link OwnerIdentities#identityKey(Owner)}, so an owner is a duplicate only when all
 * three components agree. Because the telephone is part of the key, two owners with the same last
 * name and postcode but a different telephone have different keys and are both allowed (they become a
 * soft match, see {@link CheckPossibleDuplicate}); only an exact key match is rejected with 409 (see
 * {@link DuplicateIdentityException}). This single key subsumes the former household-duplicate block.
 *
 * <p>A request may opt in with {@code sharesHousehold=true} to declare that it intentionally joins an
 * existing household; doing so <em>bypasses this block</em> entirely.
 *
 * <p>The email-domain blocklist is applied earlier by {@code ValidateOwnerFields} (a 400 before this
 * step runs), so any request reaching here already carries an allowed email.
 *
 * <p>Runs after {@link BuildOwner} so the new owner is built, and before {@link SaveOwner} so the new
 * owner is not compared against itself.
 */
public class CheckUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member: bypass the duplicate block.
        }
        String identityKey = OwnerIdentities.identityKey(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never collide with self (should not yet be persisted, but be safe)
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner no longer blocks a new registration.
            }
            if (identityKey.equals(OwnerIdentities.identityKey(existing))) {
                throw new DuplicateIdentityException(owner.getHouseholdId());
            }
        }
    }
}

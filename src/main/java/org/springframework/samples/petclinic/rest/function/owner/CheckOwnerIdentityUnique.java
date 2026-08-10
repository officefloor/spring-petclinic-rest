package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerIdentityConflictException;

/**
 * Consolidated duplicate check for {@code POST /api/owners}. Rejects with 409
 * ({@link OwnerIdentityConflictException}) only when the new owner's whole {@code identityKey} — the
 * SHA-256 over normalized telephone {@code '|'} email {@code '|'} soundex(lastName), see
 * {@link Owner#getIdentityKey()} — exactly matches an existing owner's. Because the telephone is part
 * of the key, two members of the same household (same lastName and postcode) with different
 * telephones have different keys and are both allowed — the later one is instead flagged a soft match
 * by {@link AssignPossibleDuplicate}. The email-domain blocklist is applied earlier during field
 * validation, so a disposable-domain email is rejected before this check runs.
 *
 * <p>The {@code sharesHousehold} request flag bypasses this block: when set, the new owner is
 * created as a <em>declared</em> household member rather than rejected (and is not flagged a possible
 * duplicate — see {@link AssignPossibleDuplicate}). Runs before {@link SaveOwner} (so the new owner
 * is not yet in the repository scan).
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerIdentityConflictException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member: bypass the duplicate block
        }
        String identityKey = owner.getIdentityKey();
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner never blocks a create
            }
            if (identityKey.equals(existing.getIdentityKey())) {
                throw new OwnerIdentityConflictException(identityKey);
            }
        }
    }
}

package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.util.OwnerIdentities;

/**
 * Rejects a create-owner request that exactly duplicates an existing owner's identity. The identity
 * is the full {@code identityKey} — {@code telephone + email + householdId} — computed by
 * {@link OwnerIdentities#identityKey(Owner)}, so an owner is a duplicate only when its <em>whole</em>
 * key matches. Because the telephone is part of the key, two members of the same household (same
 * {@code householdId}) with different telephones have different keys and are both allowed; only an
 * exact full-key match is rejected with 409 (see {@link DuplicateIdentityException}).
 *
 * <p>A request may opt in with {@code sharesHousehold=true} to declare that it intentionally joins an
 * existing household; doing so <em>bypasses this block</em> entirely.
 *
 * <p>Runs after {@link BuildOwner} and {@link AssignHousehold} so the new owner's {@code householdId}
 * is already assigned, and before {@link SaveOwner} so the new owner is not compared against itself.
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

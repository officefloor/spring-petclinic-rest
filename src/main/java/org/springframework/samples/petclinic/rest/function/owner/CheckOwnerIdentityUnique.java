package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.IdentityKey;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Rejects a create-owner request whose derived {@code identityKey} exactly equals an existing
 * owner's. The key is {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}
 * (see {@link IdentityKey}); this single rule now expresses what were previously the separate
 * telephone, email and household duplicate checks. Only a whole-key match is a duplicate — two
 * members of the same household (same householdId) with different telephones have different
 * keys and are both allowed.
 *
 * <p>Runs after {@link AssignHousehold} (so the built owner's householdId is settled) and
 * before {@link SaveOwner} (so the new owner is not yet among {@code findAll()}). On a match
 * raises {@link DuplicateIdentityException} (409).
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner built, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = IdentityKey.of(built);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(IdentityKey.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}

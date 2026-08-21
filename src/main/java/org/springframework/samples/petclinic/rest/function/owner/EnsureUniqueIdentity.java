package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The single, consolidated duplicate check: rejects a create request whose whole
 * {@code identityKey} (see {@link OwnerIdentity}) equals an existing owner's, so a duplicate
 * is a 409. This one key subsumes the former separate telephone, email and household checks —
 * because the telephone is part of the key, two members of the same household with different
 * telephones have different keys and are both allowed; only an exact whole-key match collides.
 *
 * <p>Runs after {@link AssignHousehold} so the new owner's {@code householdId} (the last key
 * component) is set before the key is formed. The new owner is not yet saved, so it is not
 * among the existing owners compared against here.
 */
public class EnsureUniqueIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentity.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(OwnerIdentity.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}

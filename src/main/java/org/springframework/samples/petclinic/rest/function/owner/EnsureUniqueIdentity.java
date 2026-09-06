package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The single owner duplicate block, expressed through the {@link OwnerIdentity#key(Owner) identityKey}
 * (SHA-256 over normalized telephone + lower-case email + soundex of the last name). A second owner
 * whose whole key matches an existing one is a duplicate and is rejected (409); because the telephone
 * is part of the key, two owners with the same last name and postcode but different telephones have
 * distinct keys and are both allowed (they become a soft match — see {@link AssignPossibleDuplicate}).
 * The email-domain blocklist is applied earlier, in {@link BuildOwner}.
 *
 * <p>The request's {@code sharesHousehold} flag bypasses this block: a declared household member is
 * created even when its identity key already exists. Soft-deleted owners are ignored; existing owners
 * are compared by their own computed key.
 */
public class EnsureUniqueIdentity {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return; // declared household member: allowed to join an existing household
        }
        String identityKey = OwnerIdentity.key(owner);
        boolean inUse = ownerRepository.findAll().stream()
                .filter(existing -> !existing.getId().equals(owner.getId()))
                .filter(existing -> !Boolean.TRUE.equals(existing.getDeleted())) // ignore soft-deleted owners
                .anyMatch(existing -> identityKey.equals(OwnerIdentity.key(existing)));
        if (inUse) {
            throw new DuplicateIdentityException(
                    "An owner with identity key " + identityKey + " already exists");
        }
    }
}

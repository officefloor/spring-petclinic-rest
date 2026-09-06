package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The owner duplicate block, expressed through the {@link OwnerIdentity#key(Owner) identityKey}
 * (normalized telephone + email + householdId). A second owner whose whole key matches an existing
 * one is a duplicate and is rejected (409); because the telephone is part of the key, two members of
 * the same household with different telephones have distinct keys and are both allowed.
 *
 * <p>The request's {@code sharesHousehold} flag bypasses this block: a declared household member is
 * created even when its identity key already exists. Runs after {@link AssignHousehold} so the new
 * owner's {@code householdId} (and hence its identity key) is set; existing owners are compared by
 * their own computed key.
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

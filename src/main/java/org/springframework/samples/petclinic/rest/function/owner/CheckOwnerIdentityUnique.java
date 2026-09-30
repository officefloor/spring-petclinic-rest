package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The single duplicate check for creating an owner: rejects 409 when the new owner's whole
 * {@link Owner#getIdentityKey() identityKey} — {@code normalizedTelephone|email|householdId} —
 * equals an existing owner's. This consolidates the former separate telephone, email and household
 * checks into one derived key, so a create is a duplicate only on an exact full-key match. Because
 * the telephone is part of the key, two members of the same household (same {@code householdId})
 * with different telephones have different keys and are both allowed.
 *
 * <p>Runs after {@link BuildOwner} and {@link AssignOwnerHousehold} have produced the entity and
 * resolved its {@code householdId}, so both sides of the comparison are reduced to canonical form
 * through the same {@link Owner#getIdentityKey()} and can never drift. The new owner is not yet
 * persisted, so {@link OwnerRepository#findAll()} yields only the other owners.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = owner.getIdentityKey();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue;
            }
            if (identityKey.equals(existing.getIdentityKey())) {
                throw new DuplicateIdentityException(
                        "An owner with the same identity key already exists");
            }
        }
    }
}

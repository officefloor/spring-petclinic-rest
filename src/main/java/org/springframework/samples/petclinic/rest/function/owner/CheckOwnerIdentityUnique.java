package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.IdentityKey;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerIdentityConflictException;

/**
 * The single duplicate-detection step. Rejects a create request whose owner has the same
 * {@code identityKey} as an existing owner. The key consolidates what used to be three
 * separate checks (telephone, email and household), so a create is a duplicate only when
 * its <em>whole</em> key matches — telephone <em>and</em> email <em>and</em> household.
 *
 * <p>Runs after {@link BuildOwner} and {@link AssignHousehold} so the new owner carries
 * its normalized telephone/email and its assigned {@code householdId}, and before
 * {@link SaveOwner} so a duplicate is a 409 rather than a persisted record. Because the
 * telephone is part of the key, two members of the same household with different
 * telephones have different keys and are both allowed.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerIdentityConflictException {
        String identityKey = IdentityKey.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(IdentityKey.of(existing))) {
                throw new OwnerIdentityConflictException(identityKey);
            }
        }
    }
}

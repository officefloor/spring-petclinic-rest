package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The single duplicate check for creating an owner: rejects a request whose derived
 * {@link Owner#getIdentityKey() identity key} — normalized telephone, email and household id — equals
 * that of an existing owner. It subsumes the former separate telephone, email and household checks:
 * because the telephone is part of the key, two members of one household with different telephones
 * have different keys and are both allowed; only an exact full-key match is a duplicate.
 *
 * <p>Runs after {@link AssignHouseholdId} so the new owner carries its final {@code householdId}, and
 * before {@link SaveOwner} so a collision is rejected with 409 before anything is persisted. Only
 * owners sharing the (already normalized) telephone can share the whole key, so those are the
 * candidates compared.
 */
public class EnsureUniqueIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateIdentityException {
        String identityKey = owner.getIdentityKey();
        for (Owner existing : ownerRepository.findByTelephone(owner.getTelephone())) {
            if (!existing.isDeleted() && identityKey.equals(existing.getIdentityKey())) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}

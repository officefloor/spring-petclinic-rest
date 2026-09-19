package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityKeyException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request whose identity key already belongs to another owner,
 * responding 409. The identity key (see {@link Owner#getIdentityKey()}) is the single
 * duplicate-detection rule that consolidates the former separate telephone, email and
 * household checks: an owner is a duplicate only when its WHOLE key matches an existing
 * one, so owners that share a household but differ in telephone or email are all allowed.
 * Runs after the telephone and email are normalized ({@link NormalizeOwnerTelephone} /
 * {@link NormalizeOwnerEmail}) and the household id is assigned ({@link AssignHouseholdId}),
 * so the comparison is between fully derived keys, and before {@link SaveOwner} persists a
 * duplicate. Candidates are narrowed by telephone, which every equal key necessarily
 * shares.
 */
public class EnsureUniqueIdentityKey {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityKeyException {
        String identityKey = owner.getIdentityKey();
        for (Owner existing : ownerRepository.findByTelephone(owner.getTelephone())) {
            if (identityKey.equals(existing.getIdentityKey())) {
                throw new DuplicateIdentityKeyException(identityKey);
            }
        }
    }
}

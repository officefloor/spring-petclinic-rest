package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerIdentityDuplicateException;

/**
 * The single duplicate-detection step: rejects the create only when the new owner's WHOLE
 * derived {@link OwnerIdentity#identityKey identityKey} (telephone|email|householdId) equals
 * an existing owner's. This consolidates the former separate telephone, email and household
 * duplicate checks into one key.
 *
 * <p>Runs after {@link AssignHouseholdId}, so the new owner already carries its
 * {@code householdId} and every existing household member has been back-filled with it — the
 * household segment of both keys is therefore in sync. Because the telephone is part of the
 * key, two members of the same household with different telephones have different keys and are
 * both allowed; only an exact full-key match is rejected 409 via
 * {@link OwnerIdentityDuplicateException}.
 */
public class RequireUniqueOwnerIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerIdentityDuplicateException {
        String key = OwnerIdentity.identityKey(owner.getTelephone(), owner.getEmail(),
                owner.getHouseholdId());
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // never compare the new owner against itself
            }
            String other = OwnerIdentity.identityKey(existing.getTelephone(), existing.getEmail(),
                    existing.getHouseholdId());
            if (key.equals(other)) {
                throw new OwnerIdentityDuplicateException(key);
            }
        }
    }
}

package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerIdentityConflictException;

/**
 * The single, consolidated duplicate check for {@code POST /api/owners}: rejects the request with
 * 409 when the new owner's whole {@link OwnerIdentityKey identityKey}
 * ({@code normalizedTelephone|email|householdId}) equals an existing owner's. This replaces the
 * former separate telephone, email and household checks; because the telephone is part of the key,
 * two members of one household (same householdId) with different telephones have different keys and
 * are both allowed — only an exact full-key match is a duplicate.
 *
 * <p>Runs after {@link AssignHouseholdId} so the householdId segment is populated before the key is
 * built, and before {@link SaveOwner} so the not-yet-saved owner is not compared against itself.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerIdentityConflictException {
        String identityKey = OwnerIdentityKey.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue;
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner no longer blocks a new identity
            }
            if (identityKey.equals(OwnerIdentityKey.of(existing))) {
                throw new OwnerIdentityConflictException(
                        "An owner with identity " + identityKey + " already exists");
            }
        }
    }
}

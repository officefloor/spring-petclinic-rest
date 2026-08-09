package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects creating an owner whose derived {@code identityKey} exactly matches an existing owner's.
 *
 * <p>The key — {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId} — is the one
 * place duplicate detection now lives, replacing the former separate telephone, email and household
 * checks. Because the telephone is part of the key, two members of the same household (same
 * {@code householdId}) with different telephones have different keys and are both allowed; only an
 * exact full-key match is a duplicate.
 *
 * <p>Runs after {@link NormalizeOwnerTelephone}/{@link NormalizeOwnerEmail} (so the key uses
 * canonical values) and after {@link AssignHouseholdId} (so the {@code householdId} component is
 * populated), and before {@link SaveOwner} so a duplicate is a 409 (Conflict) rather than a
 * persisted row.
 */
public class EnsureOwnerIdentityUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = owner.getIdentityKey();
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue;
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner is treated as absent
            }
            if (identityKey.equals(existing.getIdentityKey())) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}

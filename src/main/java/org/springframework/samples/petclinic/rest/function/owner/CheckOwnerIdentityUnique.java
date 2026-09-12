package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.IdentityKey;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerIdentityConflictException;

/**
 * The duplicate-detection block, now keyed on the single {@link IdentityKey}: a create is
 * rejected (409) only when its {@code identityKey} — the SHA-256 over
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)} — equals that of
 * an existing owner. Soft-deleted owners are ignored, so they no longer block a duplicate.
 * The email-domain blocklist has already been applied (an invalid/disposable email is a 400
 * in {@link ValidateOwnerFields}), so this runs against a valid, normalized identity.
 *
 * <p>Because the telephone is part of the key, two owners sharing only a last name and
 * postcode with different telephones derive <em>different</em> keys — they are no longer a
 * hard household duplicate here but a soft match (see {@link AssignPossibleDuplicate}). The
 * former household-duplicate 409 keyed on {@code householdId} no longer applies.
 *
 * <p>Runs after {@link BuildOwner} (so the new owner carries its normalized telephone,
 * email and last name) and before {@link SaveOwner} (so a duplicate is a 409 rather than a
 * persisted record).
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerIdentityConflictException {
        String identityKey = IdentityKey.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer blocks a duplicate
            }
            if (identityKey.equals(IdentityKey.of(existing))) {
                throw new OwnerIdentityConflictException(identityKey);
            }
        }
    }
}

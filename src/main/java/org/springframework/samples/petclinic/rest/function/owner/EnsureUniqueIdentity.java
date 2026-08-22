package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The consolidated duplicate check, rejecting a create with 409 when the new owner's whole
 * {@code identityKey} (see {@link OwnerIdentity}, the SHA-256 hex over
 * {@code normalizedTelephone|lowerEmail|soundex(lastName)}) equals an existing, non-deleted owner's.
 *
 * <p>This single identity key is now the whole of duplicate detection: the separate household
 * duplicate block (keyed on the computed {@code householdId}) no longer applies. Because the
 * telephone is part of the key, two owners sharing {@code soundex(lastName)} and postcode but with
 * different telephones have different keys and are <em>not</em> rejected here — they are created as a
 * soft match (see {@link AssignPossibleDuplicate}). Only an exact whole-key match collides.
 *
 * <p>Soft-deleted owners are ignored, so a re-create after a delete succeeds. The blocklisted
 * email-domain rejection runs earlier (see {@code ValidateOwnerFields}), before this step. The new
 * owner is not yet saved, so it is not among the existing owners compared here.
 */
public class EnsureUniqueIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentity.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer blocks a create
            }
            if (identityKey.equals(OwnerIdentity.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}

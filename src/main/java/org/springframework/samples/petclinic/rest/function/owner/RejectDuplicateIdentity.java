package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Hard duplicate detection on create: rejects (409) an owner whose {@code identityKey} equals that
 * of an existing owner. The single identity key (SHA-256 over normalized telephone, lower-cased
 * email and the last name's Soundex — see {@link OwnerIdentity}) is the whole definition of a
 * duplicate; there is no separate household-duplicate block.
 *
 * <p>Owners flagged {@code deleted} are ignored, so a soft-deleted record never blocks a recreate.
 * Runs after the email-domain blocklist (see {@link NormalizeOwnerEmail}) and after the telephone
 * has been normalized, so the key is computed over the same normalized inputs as the stored owners
 * it is compared against. Because the telephone is part of the key, two owners sharing a last name
 * and postcode but carrying different telephones are not hard duplicates — they fall through to the
 * soft signal (see {@link FlagPossibleDuplicate}).
 */
public class RejectDuplicateIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateOwnerException {
        String identityKey = OwnerIdentity.identityKey(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() == null || existing.getId().equals(owner.getId())) {
                continue;
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                // A soft-deleted owner is not a duplicate.
                continue;
            }
            if (identityKey.equals(OwnerIdentity.identityKey(existing))) {
                throw new DuplicateOwnerException("An owner with the same identity already exists");
            }
        }
    }
}

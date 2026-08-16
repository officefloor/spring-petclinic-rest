package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Rejects creating an owner that duplicates an existing one, so the create endpoint responds 409
 * instead of storing a duplicate. Duplicate detection is the single identity key: the new owner's
 * identity key (the SHA-256 over {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)},
 * see {@link OwnerIdentity}) equals an existing owner's. This is genuinely the same person and is
 * always rejected.
 *
 * <p>Because the telephone is part of the key, two owners with the same last name and postcode but
 * different telephones have different keys and are <em>not</em> rejected — the second is created as a
 * soft match (see {@link PossibleDuplicate}). There is no separate household-duplicate rule.
 *
 * <p>Runs after {@link ValidateOwner} (which normalizes and publishes the body, and applies the
 * email-domain blocklist first) and before {@link BuildOwner}, comparing against every existing owner
 * and ignoring any flagged deleted.
 */
public class CheckIdentityUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentity.key(request.getTelephone(), request.getEmail(),
                request.getLastName());
        for (Owner existing : ownerRepository.findAll()) {
            // A soft-deleted owner is not a live duplicate: skip it so its identity frees up.
            if (existing.isDeleted()) {
                continue;
            }
            if (identityKey.equals(OwnerIdentity.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}

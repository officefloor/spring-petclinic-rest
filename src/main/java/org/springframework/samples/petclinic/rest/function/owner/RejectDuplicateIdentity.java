package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.function.common.IdentityKeys;

/**
 * The duplicate-detection step of {@code POST /api/owners}. Runs after {@link ValidateOwnerFields}
 * has normalized the telephone and email (and after {@link RejectDisposableEmailDomain} has applied
 * the email-domain blocklist). It rejects the create with a 409 Conflict when the request's whole
 * {@code identityKey} — the {@code SHA-256} of
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}, see {@link IdentityKeys} —
 * equals a live owner's.
 *
 * <p>Duplicate detection is the single identity key: there is no separate household-duplicate block.
 * Because the telephone is part of the key, two owners with the same last name and postcode but
 * different telephones have different keys, so the second is created (and later flagged a possible
 * duplicate by {@link AssignPossibleDuplicate}) rather than rejected. Soft-deleted owners are ignored
 * — a retired identity cannot block a create.
 */
public class RejectDuplicateIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = IdentityKeys.of(request.getTelephone(), request.getEmail(),
                request.getLastName());
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                // A soft-deleted owner is no longer a live identity; it cannot block a create.
                continue;
            }
            if (identityKey.equals(IdentityKeys.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}

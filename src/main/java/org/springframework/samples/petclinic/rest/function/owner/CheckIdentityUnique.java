package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The single duplicate check for the create-owner endpoint. It rejects (409) when a new owner's
 * derived {@code identityKey} — the SHA-256 hex of {@code normalizedTelephone|lowerEmail|soundex(lastName)}
 * (see {@link OwnerIdentity}, also returned on the owner) — equals an existing owner's. This is now the
 * only hard-duplicate check: the former separate household-duplicate block no longer applies.
 *
 * <p>Because the normalized telephone is part of the hashed key, two owners sharing a last name and
 * postcode but with different telephones have different keys and both create (the second flagged a soft
 * match, see {@link AssignPossibleDuplicate}) — a change from the former household check, which rejected
 * them regardless of telephone. Soft-deleted owners are ignored, and the email-domain blocklist has
 * already run in {@link ValidateOwnerFields} (a rejected email is a 400 before this check).
 *
 * <p>Runs after {@link ValidateOwnerFields} has normalized the telephone (E.164) and email
 * (lower-cased), and within the write transaction so the check and the insert see one consistent view.
 */
public class CheckIdentityUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentity.identityKey(
                request.getTelephone(), request.getEmail(), request.getLastName());
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // soft-deleted owners are ignored by the identity check
            }
            String existingKey = OwnerIdentity.identityKey(
                    existing.getTelephone(), existing.getEmail(), existing.getLastName());
            if (identityKey.equals(existingKey)) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}

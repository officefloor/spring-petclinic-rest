package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Step of {@code POST /api/owners} that consolidates duplicate detection into the single
 * {@link OwnerIdentityKey}. Runs after {@link RequireOwnerFields} has normalized the body (and
 * already applied the disposable email-domain blocklist) and rejects the create with 409 when the
 * new owner's WHOLE identityKey — the SHA-256 hex of
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)} — equals an existing,
 * non-deleted owner's.
 *
 * <p>Duplicate detection is now this one key: an owner sharing another's surname and postcode but
 * with a different telephone has a different key and is no longer a hard household duplicate — it is
 * created and flagged a soft match by {@link AssignPossibleDuplicate}. Soft-deleted owners never
 * block a create.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentityKey.forRequest(request);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner never blocks a create
            }
            if (identityKey.equals(OwnerIdentityKey.forOwner(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}

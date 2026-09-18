package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerIdentityException;

/**
 * Runs in {@code POST /api/owners} after {@link ValidateOwnerFields} (and after the email-domain
 * blocklist), reading the already-normalized body as a variable. Rejects a duplicate — an existing
 * owner with the same {@link OwnerIdentity#key identity key} (normalized telephone, lower-cased
 * email and {@code soundex(lastName)}) — with a 409, before any entity is built or persisted.
 *
 * <p>The identity key is the single duplicate criterion: because the telephone is part of it, two
 * owners with the same last name and postcode but different telephones have different keys and are
 * both allowed — the second is created and flagged downstream as a possible duplicate by
 * {@link AssignOwnerPossibleDuplicate}. A soft-deleted owner no longer holds its identity and is
 * ignored. Reads the same transaction as the writes.
 */
public class EnsureUniqueOwnerIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerIdentityException {

        String identityKey = OwnerIdentity.key(request.getTelephone(), request.getEmail(), request.getLastName());
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer holds its identity
            }
            String existingKey = OwnerIdentity.key(existing.getTelephone(), existing.getEmail(),
                    existing.getLastName());
            if (identityKey.equals(existingKey)) {
                throw new DuplicateOwnerIdentityException(identityKey);
            }
        }
    }
}

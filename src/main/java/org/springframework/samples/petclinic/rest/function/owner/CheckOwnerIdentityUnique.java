package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerIdentityException;

/**
 * The duplicate block for a create-owner request. It rejects a create with a 409 only for an
 * <b>exact identity duplicate</b> — an existing owner with the same whole
 * {@link OwnerIdentityKey identity key} ({@code SHA-256(normalizedTelephone + '|' + lowerEmail +
 * '|' + soundex(lastName))}). This is the single identity check: because the telephone and email
 * are part of the key, two people with the same (phonetic) last name and postcode but a different
 * telephone or email have distinct keys and are both allowed — there is no separate
 * household-duplicate rejection.
 *
 * <p>A genuine second person sharing a household is instead surfaced as a soft
 * {@code possibleDuplicate} by {@link FlagOwnerPossibleDuplicate} (matching on soundex(lastName)
 * and postcode) unless the request declares the shared household with {@code sharesHousehold} true.
 * Soft-deleted owners never block a create.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerIdentityException {
        String identityKey = OwnerIdentityKey.of(request.getTelephone(), request.getEmail(),
                request.getLastName());
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner never blocks a create
            }
            if (identityKey.equals(OwnerIdentityKey.forOwner(existing))) {
                throw new DuplicateOwnerIdentityException(identityKey);
            }
        }
    }
}

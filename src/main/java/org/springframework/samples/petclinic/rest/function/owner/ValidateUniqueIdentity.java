package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The single duplicate-detection gate of {@code POST /api/owners}, running before {@link BuildOwner}.
 * Duplicate detection is now the one identity key (see {@link OwnerIdentity}): the create is rejected
 * with a 409 when an existing active owner carries the identical identity key, derived from the
 * request's normalized telephone, lower-cased email and {@link Soundex soundex} of the last name.
 *
 * <p>The scan ignores owners flagged soft-deleted (see {@link ActiveOwners}), and the email-domain
 * blocklist is applied earlier in {@link ValidateNewOwner}, so a disposable-domain email is a 400
 * before this gate runs. Because the telephone is part of the key, two owners with the same last
 * name and postcode but different telephones are no longer a hard duplicate — they are created and
 * surfaced as a soft match (see {@link PossibleDuplicate}). Publishes nothing; it only escalates on
 * conflict.
 */
public class ValidateUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String key = OwnerIdentity.of(request.getTelephone(), request.getEmail(), request.getLastName());
        for (Owner existing : ActiveOwners.list(ownerRepository)) {
            if (key.equals(OwnerIdentity.of(existing))) {
                throw new DuplicateIdentityException(
                        "An owner with the same identity key already exists: " + key);
            }
        }
    }
}

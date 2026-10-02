package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The single duplicate check for a new owner, keyed on its {@link Owner#identityKey identity key} — the
 * SHA-256 hex over {@code normalizedTelephone | lowerEmail | soundex(lastName)}. A new owner whose
 * identity key equals that of an existing, non-deleted owner is the same identity and is rejected with a
 * 409 Conflict ({@link DuplicateIdentityException}).
 *
 * <p>Because the telephone is part of the key, two owners sharing a last name and postcode but with
 * different telephones have <em>different</em> identity keys: they are not a hard duplicate here (they are
 * instead flagged as a soft near-match by {@link FlagPossibleDuplicate}). Soft-deleted owners are ignored,
 * so a matching identity that has been deleted no longer blocks a create.
 *
 * <p>Runs after {@link ValidateNewOwner} (so the telephone is E.164 and the email lower-cased) and
 * {@link ValidateOwnerPostcode}, and before {@link BuildOwner}. The email-domain blocklist is applied
 * first, in {@link ValidateNewOwner}. Existing owners are compared by their recomputed identity key, so
 * seed data collides with an equivalent request.
 */
public class CheckIdentityUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = Owner.identityKey(request.getTelephone(), request.getEmail(), request.getLastName());
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer blocks a matching identity
            }
            if (identityKey.equals(existing.getIdentityKey())) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}

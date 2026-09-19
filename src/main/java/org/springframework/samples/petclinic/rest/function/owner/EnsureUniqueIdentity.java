package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request whose derived {@link OwnerIdentity identity key} exactly matches an
 * existing owner's, so the pair maps to a 409. This is the single duplicate check for owner
 * creation, consolidating the former separate telephone, email and household guards: only an
 * exact whole-key match is a duplicate, so two members of the same household with different
 * telephones (different keys) are both allowed.
 *
 * <p>Runs after the request's telephone and email have been normalized, and before
 * {@link BuildOwner} maps it to an entity. Existing owners are compared on their recomputed key
 * too, so formatting differences do not hide a collision.
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentity.of(request);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(OwnerIdentity.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}

package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The single duplicate check for a new owner. All duplicate detection is consolidated into one derived
 * {@code identityKey} (see {@link OwnerIdentity}): a new owner is rejected with a 409 Conflict only when
 * its WHOLE identity key equals an existing owner's. This replaces the separate telephone, email and
 * household checks.
 *
 * <p>The {@code householdId} component of the key is derived and assigned only once an owner is created
 * ({@link AssignHousehold} runs after this check), so at this point it is not yet known: both the
 * candidate and the owners it is compared against contribute an empty household component. Because the
 * normalized telephone is part of the key, two members of the same household with different telephones
 * still produce different keys and are both allowed; only an exact match of the key as known at creation
 * time is a duplicate.
 *
 * <p>Runs after {@link ValidateNewOwner} (which has normalized the request telephone to E.164 and
 * lower-cased the email) and before {@link BuildOwner}. Existing owners are compared in their canonical
 * form so seed data that was not normalized still collides with an equivalent request.
 */
public class CheckIdentityUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        // householdId is assigned only on successful creation, so it is not part of the key here.
        String identityKey = OwnerIdentity.of(request.getTelephone(), request.getEmail(), null);
        for (Owner existing : ownerRepository.findAll()) {
            String existingKey = OwnerIdentity.of(existing.getTelephone(), existing.getEmail(), null);
            if (identityKey.equals(existingKey)) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}

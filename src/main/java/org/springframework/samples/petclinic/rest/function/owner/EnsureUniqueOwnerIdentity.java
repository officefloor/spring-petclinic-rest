package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request whose derived {@link OwnerIdentityKey} exactly matches an
 * existing owner's, responding 409. This is the single consolidated duplicate check, replacing
 * the former separate telephone, email and household checks: because the telephone is part of
 * the key, two members of the same household (same householdId) with different telephones have
 * different keys and are both allowed — only an exact full-key match is a duplicate.
 *
 * <p>Runs after {@link ValidateOwnerFields} (which normalizes the telephone to E.164 and
 * lower-cases the email) and before {@link BuildOwner}. The request's household is the one it
 * would resolve to against existing owners (see {@link OwnerHousehold#resolve}), so the key
 * matches the {@code householdId} later assigned by {@link AssignOwnerHousehold}.
 */
public class EnsureUniqueOwnerIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerIdentityException {
        String householdId = OwnerHousehold.resolve(request.getLastName(), request.getAddress(), ownerRepository);
        String identityKey = OwnerIdentityKey.of(request.getTelephone(), request.getEmail(), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(OwnerIdentityKey.of(existing))) {
                throw new DuplicateOwnerIdentityException(identityKey);
            }
        }
    }
}

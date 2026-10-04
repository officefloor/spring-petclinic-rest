package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Create-time uniqueness guard for {@code POST /owners}: the single duplicate check. It rejects a
 * request whose whole {@link OwnerIdentity identityKey} equals an existing owner's, with a 409 via
 * {@link DuplicateIdentityException}. This one key subsumes the former separate telephone, email
 * and household guards: a collision requires the normalised telephone, the email and the household
 * to all match, so (for example) two members of one household with different telephones are both
 * allowed.
 *
 * <p>Runs after {@link NormalizeOwnerTelephone} and {@link NormalizeOwnerEmail} so the request's
 * telephone and email are already in their stored (canonical) forms, and before {@link BuildOwner}.
 * A new owner's {@code householdId} is not assigned until later in the pipeline, so the request's
 * household component is empty here; it is compared against each existing owner's stored key.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentity.key(request.getTelephone(), request.getEmail(), null);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(OwnerIdentity.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}

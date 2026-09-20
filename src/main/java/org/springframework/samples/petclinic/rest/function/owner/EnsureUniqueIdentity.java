package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The duplicate block: rejects a create request whose {@link IdentityKeys identity key} — its
 * normalized telephone, lower-cased email and last-name Soundex — matches an existing owner's,
 * because that is a re-registration of the same identity. Owners flagged deleted are ignored.
 * The email-domain blocklist runs earlier in the pipeline, so a blocked domain is rejected
 * before this check. Owners in one household with different telephones no longer collide here;
 * they are surfaced as a soft match instead (see {@link DetectPossibleDuplicate}).
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = IdentityKeys.of(request);
        for (Owner existing : ownerRepository.findAllActive()) {
            if (identityKey.equals(IdentityKeys.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}

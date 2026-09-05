package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Consolidated duplicate check replacing the former separate telephone, email and
 * household checks, run before {@link BuildOwner}. Duplicate detection now turns on the
 * derived {@link IdentityKey} (SHA-256 over normalized telephone, lower email and
 * soundex(lastName)): two owners collide exactly when that whole key matches. Because the
 * telephone is part of the key, same-household owners with different telephones have
 * different keys and are both allowed (a soft match, not a 409). Owners flagged deleted are
 * ignored; the email-domain blocklist has already been applied by the earlier email step.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String key = IdentityKey.of(request.getTelephone(), request.getEmail(),
                request.getLastName(), request.getPostcode());
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue;
            }
            if (key.equals(IdentityKey.of(existing.getTelephone(), existing.getEmail(),
                    existing.getLastName(), existing.getPostcode()))) {
                throw new DuplicateTelephoneException(request.getTelephone());
            }
        }
    }
}

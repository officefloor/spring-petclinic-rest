package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Consolidated duplicate check replacing the former separate telephone, email and
 * household checks, run before {@link BuildOwner}. Duplicate detection now turns on the
 * derived {@link IdentityKey} whose leading, always-present segment is the normalized
 * telephone: two owners collide exactly when that telephone matches. Because the
 * telephone is part of the key, same-household owners with different telephones have
 * different keys and are both allowed. The full key is exposed on the owner response.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue;
            }
            if (telephone.equals(existing.getTelephone())) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}

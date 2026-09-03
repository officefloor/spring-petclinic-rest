package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerTelephoneException;

/**
 * Consolidated duplicate guard: rejects a create request whose WHOLE {@code identityKey} equals an
 * existing owner's (see {@link OwnerIdentity}). Telephone is part of the key, so household members
 * with different telephones have different keys and are both allowed; only an exact full-key match is
 * a duplicate. A collision necessarily shares the telephone, so it reports 409 via
 * {@link DuplicateOwnerTelephoneException}.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerTelephoneException {
        String key = OwnerIdentity.key(request.getTelephone(), request.getEmail(), request.getLastName());
        for (Owner owner : ownerRepository.findAll()) {
            if (owner.isDeleted()) {
                continue;
            }
            if (key.equals(OwnerIdentity.key(owner.getTelephone(), owner.getEmail(), owner.getLastName()))) {
                throw new DuplicateOwnerTelephoneException(request.getTelephone());
            }
        }
    }
}

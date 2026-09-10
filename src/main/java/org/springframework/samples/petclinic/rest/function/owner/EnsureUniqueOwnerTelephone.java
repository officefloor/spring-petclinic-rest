package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request whose E.164 telephone is already used by any other owner,
 * responding 409. Runs after {@link ValidateOwnerFields} (which normalizes the telephone to
 * E.164 form) and before {@link BuildOwner}, comparing E.164 values against every existing
 * owner.
 */
public class EnsureUniqueOwnerTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerTelephoneException {
        String telephone = OwnerTelephone.canonical(request.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(OwnerTelephone.canonical(existing.getTelephone()))) {
                throw new DuplicateOwnerTelephoneException(telephone);
            }
        }
    }
}

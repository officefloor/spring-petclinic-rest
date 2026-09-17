package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Create-owner step: rejects the request with a 409 when its normalized telephone is
 * already used by any existing owner. Runs after {@link NormalizeOwnerTelephone} (so the
 * request holds the normalized value) and before {@link BuildOwner}, comparing against
 * every stored telephone in its normalized form.
 */
public class EnsureUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = NormalizeOwnerTelephone.digitsOnly(request.getTelephone());
        for (Owner owner : ownerRepository.findAll()) {
            if (telephone.equals(NormalizeOwnerTelephone.digitsOnly(owner.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}

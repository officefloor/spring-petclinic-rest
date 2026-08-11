package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Third step of {@code POST /api/owners}: rejects the request 409 when its (already normalised)
 * telephone matches the E.164 telephone of any existing owner. Runs after
 * {@link NormalizeOwnerTelephone} so both sides are compared as E.164 values, and before
 * {@link BuildOwner} so no owner is persisted on conflict.
 */
public class RequireUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(E164Telephone.normalize(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}

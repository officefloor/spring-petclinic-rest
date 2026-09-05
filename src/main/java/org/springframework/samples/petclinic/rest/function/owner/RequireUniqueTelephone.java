package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a create-owner request whose telephone is already used by any other owner,
 * before {@link BuildOwner} runs. The request telephone is already normalised to E.164
 * form by {@link RequireOwnerFields}, and existing telephones are stored the same way, so
 * the E.164 strings are compared directly.
 */
public class RequireUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(existing.getTelephone())) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}

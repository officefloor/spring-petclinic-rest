package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a create-owner request whose normalized telephone is already used by any other
 * owner, before {@link BuildOwner} runs. The request telephone is already normalised to
 * digits by {@link RequireOwnerFields}; existing telephones are normalised the same way.
 */
public class RequireUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            String other = existing.getTelephone();
            if (other != null && telephone.equals(other.replaceAll("\\D", ""))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}

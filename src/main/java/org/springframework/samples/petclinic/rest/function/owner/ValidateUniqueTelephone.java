package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Runs after {@link ValidateNewOwner} (which has normalized the telephone to its 10 digits)
 * and before {@link BuildOwner}. Rejects the create with a 409 when any existing owner shares
 * the same normalized telephone, so no two owners can share a phone number. Publishes nothing;
 * it is a validation gate that only escalates on conflict.
 */
public class ValidateUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(OwnerTelephone.normalize(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(
                        "Telephone " + telephone + " is already used by another owner");
            }
        }
    }
}

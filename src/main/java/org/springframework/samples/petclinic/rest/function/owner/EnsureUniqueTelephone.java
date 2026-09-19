package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request whose normalized telephone is already used by another owner,
 * so the pair maps to a 409. Runs after {@link NormalizeOwnerTelephone} has canonicalized
 * the request's telephone, and before {@link BuildOwner} maps it to an entity. Existing
 * owners are compared on their normalized telephone too, so formatting differences do not
 * hide a collision.
 */
public class EnsureUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(TelephoneNormalizer.digits(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}

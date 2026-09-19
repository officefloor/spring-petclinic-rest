package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request whose E.164 telephone is already used by another owner, so the
 * pair maps to a 409. Runs after {@link NormalizeOwnerTelephone} has canonicalized the
 * request's telephone to E.164, and before {@link BuildOwner} maps it to an entity. Existing
 * owners are compared on their E.164 telephone too, so formatting differences do not hide a
 * collision.
 */
public class EnsureUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(TelephoneNormalizer.toE164(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}

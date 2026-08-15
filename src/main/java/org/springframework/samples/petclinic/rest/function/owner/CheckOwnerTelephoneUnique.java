package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Rejects a create-owner request whose E.164 telephone is already used by any existing owner.
 * Runs after {@link ValidateNewOwner} has normalized the request's telephone to E.164 form, and
 * before {@link BuildOwner}, so a duplicate is a 409 rather than a persisted record. Each stored
 * owner's telephone is normalized to E.164 the same way before comparing; a stored value that
 * cannot form valid E.164 simply cannot match the (valid E.164) request telephone.
 */
public class CheckOwnerTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerTelephoneException {
        String telephone = request.getTelephone();
        for (Owner owner : ownerRepository.findAll()) {
            if (telephone.equals(normalizeOrNull(owner.getTelephone()))) {
                throw new DuplicateOwnerTelephoneException(request.getTelephone());
            }
        }
    }

    private static String normalizeOrNull(String telephone) {
        try {
            return TelephoneE164.normalize(telephone);
        }
        catch (InvalidTelephoneException ex) {
            return null;
        }
    }
}

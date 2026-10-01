package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerTelephoneDuplicateException;

/**
 * Runs after {@link ValidateOwner} (which has converted the telephone to E.164 and republished the
 * body) and before {@link BuildOwner}. Rejects the create with a 409 via
 * {@link OwnerTelephoneDuplicateException} when the E.164 telephone already belongs to any other
 * owner. Existing owners are compared on their telephone converted to E.164 the same way, so stored
 * values with incidental formatting still match.
 */
public class CheckOwnerTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerTelephoneDuplicateException {
        // ValidateOwner has already converted the request telephone to E.164; only stored owners need it.
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(OwnerTelephone.toE164(existing.getTelephone()))) {
                throw new OwnerTelephoneDuplicateException(telephone);
            }
        }
    }
}

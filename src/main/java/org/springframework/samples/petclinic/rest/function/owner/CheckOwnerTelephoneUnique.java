package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerTelephoneException;

/**
 * Rejects a create-owner request whose E.164 telephone is already used by another owner with a
 * 409. Runs after {@code NormalizeOwnerTelephone}, so the request telephone is already the E.164
 * form; each existing owner's telephone is converted to E.164 the same way before comparison.
 */
public class CheckOwnerTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(TelephoneNormalizer.toE164(existing.getTelephone()))) {
                throw new DuplicateOwnerTelephoneException(request.getTelephone());
            }
        }
    }
}

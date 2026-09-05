package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request whose normalized telephone is already used by another
 * owner. Runs after {@link NormalizeOwnerTelephone} (so the body carries the stripped
 * 10-digit value) and before {@link BuildOwner}, comparing against the normalized
 * telephone of every existing owner. A collision is rejected 409 via
 * {@link DuplicateTelephoneException}.
 */
public class RejectDuplicateTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getTelephone() != null
                    && telephone.equals(existing.getTelephone().replaceAll("\\D", ""))) {
                throw new DuplicateTelephoneException(
                        "Telephone is already used by another owner");
            }
        }
    }
}

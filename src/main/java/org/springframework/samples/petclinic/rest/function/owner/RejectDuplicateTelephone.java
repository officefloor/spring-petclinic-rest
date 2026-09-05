package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request whose E.164 telephone is already used by another owner.
 * Runs after {@link NormalizeOwnerTelephone} (so the body carries the E.164 value) and
 * before {@link BuildOwner}, comparing E.164 values against every existing owner. A
 * collision is rejected 409 via {@link DuplicateTelephoneException}.
 */
public class RejectDuplicateTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(E164Telephone.normalizeOrNull(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(
                        "Telephone is already used by another owner");
            }
        }
    }
}

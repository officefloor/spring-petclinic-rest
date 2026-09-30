package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects creating an owner whose normalized telephone is already used by any other owner.
 * Runs after {@link NormalizeOwnerTelephone} (so the request already holds the normalized
 * 10-digit value) and before {@link BuildOwner}. Both sides are reduced to digits through the
 * shared {@link NormalizeOwnerTelephone#toDigits(String)}, so the "same telephone" definition is
 * single-sourced and matches regardless of how existing values were formatted. A collision is
 * rejected 409 via {@link DuplicateTelephoneException}.
 */
public class CheckOwnerTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = NormalizeOwnerTelephone.toDigits(request.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(NormalizeOwnerTelephone.toDigits(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(
                        "Telephone " + telephone + " is already used by another owner");
            }
        }
    }
}

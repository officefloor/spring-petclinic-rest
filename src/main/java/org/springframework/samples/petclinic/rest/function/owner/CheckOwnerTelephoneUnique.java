package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Rejects creating an owner whose E.164 telephone is already used by any other owner.
 * Runs after {@link NormalizeOwnerTelephone} (so the request already holds the E.164 value) and
 * before {@link BuildOwner}. Both sides are reduced to E.164 through the shared
 * {@link NormalizeOwnerTelephone#toE164(String)}, so the "same telephone" definition is
 * single-sourced and matches regardless of how existing values were formatted. An existing value
 * that cannot form valid E.164 simply never matches. A collision is rejected 409 via
 * {@link DuplicateTelephoneException}.
 */
public class CheckOwnerTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            String existingE164;
            try {
                existingE164 = NormalizeOwnerTelephone.toE164(existing.getTelephone());
            }
            catch (InvalidTelephoneException ex) {
                continue;
            }
            if (telephone.equals(existingE164)) {
                throw new DuplicateTelephoneException(
                        "Telephone " + telephone + " is already used by another owner");
            }
        }
    }
}

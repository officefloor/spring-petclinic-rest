package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects creating an owner whose (already normalized) telephone is used by an existing owner.
 *
 * <p>Runs after {@link NormalizeOwnerTelephone} so the lookup compares canonical E.164 values, and
 * before {@link BuildOwner}/{@link SaveOwner} so a duplicate is a 409 (Conflict) rather than a
 * persisted row.
 */
public class EnsureOwnerTelephoneUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        if (!ownerRepository.findByTelephone(telephone).isEmpty()) {
            throw new DuplicateTelephoneException(telephone);
        }
    }
}

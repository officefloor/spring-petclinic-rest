package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request whose normalized telephone is already used by any existing
 * owner. Runs after {@link NormalizeOwnerTelephone} has reduced the body's telephone to
 * its digits, comparing it against every stored owner's normalized number so numbers that
 * differ only in separators are still treated as duplicates.
 */
public class EnsureUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = Telephones.normalize(request.getTelephone());
        for (Owner owner : ownerRepository.findAll()) {
            if (telephone.equals(Telephones.normalize(owner.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}

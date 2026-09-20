package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request whose E.164 telephone is already used by an existing owner. Runs
 * after {@link NormalizeOwnerTelephone} has canonicalized the body's telephone, comparing it
 * against every stored owner's E.164 number so numbers that differ only in separators or an
 * assumed country code are still treated as duplicates.
 */
public class EnsureUniqueTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        for (Owner owner : ownerRepository.findAll()) {
            if (Telephones.toE164(owner.getTelephone()).filter(telephone::equals).isPresent()) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}

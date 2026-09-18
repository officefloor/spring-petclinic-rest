package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerTelephoneException;

/**
 * Runs after {@link ValidateOwnerFields} in {@code POST /api/owners}, reading the
 * already-normalized body as a variable. Rejects the request when its telephone is already
 * used by another owner (comparing canonical E.164 telephones), throwing
 * {@link DuplicateOwnerTelephoneException} for a 409 before any entity is built or persisted.
 */
public class EnsureUniqueOwnerTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerTelephoneException {

        String telephone = request.getTelephone(); // already normalized to E.164 upstream
        for (Owner existing : ownerRepository.findAll()) {
            if (OwnerTelephone.toE164(existing.getTelephone()).map(telephone::equals).orElse(false)) {
                throw new DuplicateOwnerTelephoneException(telephone);
            }
        }
    }
}

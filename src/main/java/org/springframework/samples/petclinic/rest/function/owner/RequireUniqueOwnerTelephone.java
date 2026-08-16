package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerTelephoneDuplicateException;

/**
 * Rejects the create when the E.164 telephone published by {@link NormalizeOwnerTelephone}
 * is already used by another owner. Existing owners' telephones are converted to E.164 the
 * same way before comparison, so equivalent numbers in different formats collide.
 * A match is rejected 409 via {@link OwnerTelephoneDuplicateException}.
 */
public class RequireUniqueOwnerTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerTelephoneDuplicateException {
        String telephone = request.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            String other = NormalizeOwnerTelephone.toE164(existing.getTelephone());
            if (other != null && other.equals(telephone)) {
                throw new OwnerTelephoneDuplicateException(telephone);
            }
        }
    }
}

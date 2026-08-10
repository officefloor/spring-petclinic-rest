package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Runs after {@link NormalizeOwnerTelephone} (so the request telephone is the normalized 10-digit
 * value) and before {@link BuildOwner}. Rejects the request when any existing owner already uses the
 * same normalized telephone by throwing {@link DuplicateTelephoneException} (handled as 409). Existing
 * owners' telephones are normalized the same way before comparison so differently-formatted values
 * still collide.
 */
public class EnsureUniqueOwnerTelephone {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = request.getTelephone();
        String normalized = normalize(telephone);
        for (Owner existing : ownerRepository.findAll()) {
            if (normalized.equals(normalize(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }

    private static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}

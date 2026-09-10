package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request whose lower-cased email is already used by any other owner,
 * responding 409. Runs after {@link ValidateOwnerFields} (which lower-cases the email) and
 * before {@link BuildOwner}, comparing lower-cased values against every existing owner. A
 * request without an email is left untouched.
 */
public class EnsureUniqueOwnerEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String canonical = email.toLowerCase();
        for (Owner existing : ownerRepository.findAll()) {
            String other = existing.getEmail();
            if (other != null && canonical.equals(other.toLowerCase())) {
                throw new DuplicateOwnerEmailException(canonical);
            }
        }
    }
}

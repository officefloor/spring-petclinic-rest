package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects a create request whose email is already used by any existing owner. Runs after
 * {@link NormalizeEmail}, so the request email is already the canonical lower-cased form; each stored
 * email is lower-cased the same way before comparison. An absent or blank email is unconstrained. A
 * collision is a 409 via {@link DuplicateEmailException}.
 */
public class EnsureUniqueEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        for (Owner owner : ownerRepository.findAll()) {
            String existing = owner.getEmail();
            if (existing != null && email.equals(EmailNormalizer.normalize(existing))) {
                throw new DuplicateEmailException(email);
            }
        }
    }
}

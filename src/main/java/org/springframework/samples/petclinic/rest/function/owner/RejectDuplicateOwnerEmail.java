package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects a create-owner request whose email is already used by another owner, responding
 * 409. Runs after {@link NormalizeOwnerEmail}, so the request's email is already trimmed
 * and lower-cased (or absent). An absent email cannot collide, so the step is a no-op;
 * otherwise the normalized request email is compared against every existing owner's email
 * normalized the same way (see {@link OwnerEmails}), so the match is case-insensitive.
 */
public class RejectDuplicateOwnerEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (!OwnerEmails.isPresent(email)) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            String existingEmail = existing.getEmail();
            if (OwnerEmails.isPresent(existingEmail)
                    && email.equals(OwnerEmails.normalize(existingEmail))) {
                throw new DuplicateEmailException(email);
            }
        }
    }
}

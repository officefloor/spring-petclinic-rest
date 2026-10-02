package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Runs after {@link ValidateNewOwner} (which has lower-cased the email) and before
 * {@link BuildOwner}. Rejects the create with a 409 when any existing owner shares the same
 * lower-cased email, so no two owners can share an email address. An absent email is never a
 * conflict. Publishes nothing; it is a validation gate that only escalates on conflict.
 */
public class ValidateUniqueEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (email.equals(OwnerEmail.normalize(existing.getEmail()))) {
                throw new DuplicateEmailException(
                        "Email " + email + " is already used by another owner");
            }
        }
    }
}

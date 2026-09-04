package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerEmailException;

/**
 * Rejects a create body whose lower-cased email is already held by another owner, so
 * the endpoint responds 409. Runs after {@link NormalizeOwnerEmail} so the comparison
 * uses the lower-cased value. A body without an email is left untouched.
 */
public class RejectDuplicateOwnerEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            String other = existing.getEmail();
            if (other != null && email.equals(other.toLowerCase())) {
                throw new DuplicateOwnerEmailException(email);
            }
        }
    }
}

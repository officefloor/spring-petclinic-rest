package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects creating an owner whose lower-cased email is already used by any other owner.
 * Runs after {@link NormalizeOwnerEmail} (so the request already holds the lower-cased value) and
 * before {@link BuildOwner}. Both sides are reduced to canonical form through the shared
 * {@link NormalizeOwnerEmail#normalize(String)}, so the "same email" definition is single-sourced
 * and matches regardless of how existing values were stored. Email is optional: a request with no
 * email is never a collision, and an existing owner with no email never matches. A collision is
 * rejected 409 via {@link DuplicateEmailException}.
 */
public class CheckOwnerEmailUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (email.equals(NormalizeOwnerEmail.normalize(existing.getEmail()))) {
                throw new DuplicateEmailException(
                        "Email " + email + " is already used by another owner");
            }
        }
    }
}

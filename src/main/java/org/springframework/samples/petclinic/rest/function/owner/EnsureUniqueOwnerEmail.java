package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects creating an owner whose lower-cased email is already used by any other owner.
 * The request email and each existing owner's email are canonicalized via the shared
 * {@link OwnerEmails} rule (lower-cased) before comparing, so the check is independent of
 * the casing either value was supplied in. An absent/blank request email is never a
 * duplicate. A match is a 409 via {@link DuplicateEmailException}.
 */
public class EnsureUniqueOwnerEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = OwnerEmails.normalize(request.getEmail());
        if (email == null || email.isBlank()) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (email.equals(OwnerEmails.normalize(existing.getEmail()))) {
                throw new DuplicateEmailException(email);
            }
        }
    }
}

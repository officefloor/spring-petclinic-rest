package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request whose lower-cased email is already used by an existing owner.
 * Runs after {@link NormalizeOwnerEmail} has canonicalized the body's email, comparing it
 * against every stored owner's email in the same lower-cased form so addresses that differ
 * only in letter case are still treated as duplicates. An absent (blank) email carries no
 * identity and so is never a duplicate.
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
            if (existing != null && email.equals(Emails.normalize(existing))) {
                throw new DuplicateEmailException(email);
            }
        }
    }
}

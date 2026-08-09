package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects creating an owner whose (already lower-cased) email is used by an existing owner.
 *
 * <p>Runs after {@link NormalizeOwnerEmail} so the lookup compares canonical lower-cased values, and
 * before {@link BuildOwner}/{@link SaveOwner} so a duplicate is a 409 (Conflict) rather than a
 * persisted row. Email is optional: a missing (null) value is left untouched and never conflicts.
 */
public class EnsureOwnerEmailUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null) {
            return; // email is optional
        }
        if (!ownerRepository.findByEmail(email).isEmpty()) {
            throw new DuplicateEmailException(email);
        }
    }
}

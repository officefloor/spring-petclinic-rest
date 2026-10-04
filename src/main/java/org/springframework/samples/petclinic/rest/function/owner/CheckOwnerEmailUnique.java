package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Create-time uniqueness guard for {@code POST /owners}: rejects a request whose lower-cased email
 * is already used by any other owner, with a 409 via {@link DuplicateEmailException}.
 *
 * <p>Email is optional, so an absent (null or blank) value skips the check. Runs after
 * {@link NormalizeOwnerEmail} so the request email is already the stored lower-cased form, and
 * compares against each existing owner's email lower-cased the same way
 * ({@link OwnerEmail#normalize(String)}) so the check is independent of how existing values happen
 * to be cased. Runs before {@link BuildOwner}.
 */
public class CheckOwnerEmailUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (!OwnerEmail.isPresent(email)) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (OwnerEmail.isPresent(existing.getEmail())
                    && email.equals(OwnerEmail.normalize(existing.getEmail()))) {
                throw new DuplicateEmailException(email);
            }
        }
    }
}

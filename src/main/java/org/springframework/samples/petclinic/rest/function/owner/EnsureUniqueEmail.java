package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;
import org.springframework.util.StringUtils;

/**
 * Rejects a create-owner request whose email is already used by another owner, before
 * {@link BuildOwner} runs. Runs after {@link NormalizeOwnerEmail} so the request holds the
 * lower-cased value that matches how owner emails are persisted, making the comparison
 * lower-cased as the rule requires. Email is optional, so an absent (or blank) email is
 * left alone; a duplicate is rejected with 409.
 */
public class EnsureUniqueEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (!StringUtils.hasText(email)) {
            return;
        }
        if (!ownerRepository.findByEmail(email).isEmpty()) {
            throw new DuplicateEmailException(email);
        }
    }
}

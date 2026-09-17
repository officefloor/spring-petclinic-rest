package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailException;

/**
 * Rejects an owner whose (already-normalized) email is hosted on a disposable-mailbox
 * provider: such domains are on a blocklist and yield a 400 via
 * {@link DisposableEmailException}. An absent (null or blank) email is allowed, and
 * runs after {@link NormalizeOwnerEmail} so the domain compared is the canonical
 * lower-cased form.
 */
public class RequireOwnerEmailAllowed {

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return; // email is optional
        }
        if (OwnerEmails.isDisposableDomain(email)) {
            throw new DisposableEmailException(email);
        }
    }
}

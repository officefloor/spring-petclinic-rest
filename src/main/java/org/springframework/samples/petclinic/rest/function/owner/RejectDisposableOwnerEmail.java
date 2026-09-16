package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Rejects a create-owner request whose (already normalized) email uses a disposable-email
 * provider's domain, responding 400. An absent email is a no-op; disposable membership is
 * decided by the shared {@link OwnerEmails} rule set.
 */
public class RejectDisposableOwnerEmail {

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String email = request.getEmail();
        if (OwnerEmails.isPresent(email) && OwnerEmails.isDisposable(email)) {
            throw new DisposableEmailDomainException(OwnerEmails.domainOf(email));
        }
    }
}

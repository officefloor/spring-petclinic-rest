package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Rejects a create body whose email domain is on the disposable-domain blocklist, so the
 * endpoint responds 400. An absent or blank email is accepted. Runs after
 * {@link NormalizeOwnerEmail}, which has already lower-cased and syntax-checked the value.
 */
public class RejectDisposableEmailDomain {

    private static final Set<String> BLOCKED =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String domain = email.substring(email.indexOf('@') + 1);
        if (BLOCKED.contains(domain)) {
            throw new DisposableEmailDomainException(email);
        }
    }
}

package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Step of {@code POST /api/owners} that runs after {@link ValidateOwnerFields} has published the
 * request (with the email already normalized to lower case). Rejects the request when the email's
 * domain is on the disposable-domain blocklist, so a throwaway address is a 400 Bad Request rather
 * than a create. Email is optional; an absent or domain-less value is left for the other rules.
 */
public class RejectDisposableEmailDomain {

    /** Domains for throwaway email providers that are not accepted. */
    static final Set<String> BLOCKED_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String email = request.getEmail();
        if (email == null) {
            return;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(domain);
        }
    }
}

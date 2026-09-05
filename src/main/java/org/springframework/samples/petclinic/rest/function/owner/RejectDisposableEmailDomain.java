package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Rejects an owner request whose {@code email} domain is on the disposable-domain
 * blocklist. Runs after {@link NormalizeOwnerEmail}, which has already validated the
 * address syntactically and lower-cased it (a blank email is normalized to {@code null}
 * and skipped here). A blocklisted domain is rejected 400 via
 * {@link DisposableEmailDomainException}.
 */
public class RejectDisposableEmailDomain {

    private static final Set<String> BLOCKED_DOMAINS = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

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
            throw new DisposableEmailDomainException(
                    "Email domain is not permitted");
        }
    }
}

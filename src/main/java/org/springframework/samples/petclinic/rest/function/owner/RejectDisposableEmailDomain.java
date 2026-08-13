package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Rejects an email whose domain is on the disposable-domain blocklist. Email is optional, so an
 * absent or blank email is accepted. Runs after {@link NormalizeOwnerEmail}, which has already
 * lower-cased and syntactically validated the address, so the domain is the substring after the
 * final {@code @}. A blocklisted domain is rejected with 400 via
 * {@link DisposableEmailDomainException}.
 */
public class RejectDisposableEmailDomain {

    private static final Set<String> BLOCKED_DOMAINS = Set.of(
            "mailinator.com",
            "tempmail.com",
            "guerrillamail.com");

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return; // optional when absent
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return; // not a syntactically valid address; NormalizeOwnerEmail already guards this
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(email);
        }
    }
}

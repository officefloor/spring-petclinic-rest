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

    static final Set<String> BLOCKED_DOMAINS = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String domain = domainOf(request == null ? null : request.getEmail());
        if (domain != null && BLOCKED_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(
                    "Email domain is not permitted");
        }
    }

    /**
     * The lower-cased domain part of {@code email}, or null when there is no email or no '@'.
     */
    static String domainOf(String email) {
        if (email == null) {
            return null;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return null;
        }
        return email.substring(at + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * Whether {@code email}'s domain is <em>disposable-adjacent</em>: a subdomain of a known
     * disposable domain (e.g. {@code mail.mailinator.com}). An exact disposable domain is rejected
     * at create time by {@link #service}, so it never reaches a stored owner; the adjacency here
     * flags the softer, still-suspicious case of a subdomain that slipped past that block.
     */
    static boolean adjacent(String email) {
        String domain = domainOf(email);
        if (domain == null) {
            return false;
        }
        for (String blocked : BLOCKED_DOMAINS) {
            if (domain.endsWith("." + blocked)) {
                return true;
            }
        }
        return false;
    }
}

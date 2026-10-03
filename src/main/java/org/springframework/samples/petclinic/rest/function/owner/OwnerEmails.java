package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

/**
 * Single source of truth for what a "normalized" owner email is: lower-cased, so the
 * stored value is canonical regardless of the casing the client sent.
 * Syntactic validity is enforced upstream by the {@code @Email} constraint on the
 * request DTO; this step only canonicalizes an already-valid (or absent) address.
 * It also owns the disposable-domain blocklist (see {@link ValidateOwnerEmailDomain}).
 */
final class OwnerEmails {

    /** Known disposable-email providers; an owner email on one of these is rejected. */
    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    private OwnerEmails() {
    }

    static String normalize(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }

    /** The lower-cased domain part (after the last '@') of a normalized email, or null. */
    static String domainOf(String email) {
        String normalized = normalize(email);
        if (normalized == null) {
            return null;
        }
        int at = normalized.lastIndexOf('@');
        return at < 0 ? null : normalized.substring(at + 1);
    }

    /** True when the email's domain is on the disposable-domain blocklist. */
    static boolean isDisposableDomain(String email) {
        String domain = domainOf(email);
        return domain != null && DISPOSABLE_DOMAINS.contains(domain);
    }
}

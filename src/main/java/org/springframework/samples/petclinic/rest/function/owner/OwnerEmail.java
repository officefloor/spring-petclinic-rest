package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Single source of truth for the owner email rule, shared by the owner pipelines. An
 * owner may omit the email, but when supplied it must be a syntactically valid address
 * whose domain is not on the disposable-domain blocklist, and is stored and returned
 * lower-cased. Keeping the validity check, the blocklist and the lower-casing here keeps
 * the create and update paths consistent.
 */
final class OwnerEmail {

    /**
     * Pragmatic syntactic check for a single address: a non-empty local part, an
     * {@code @}, then a domain with at least one dot and no whitespace. Rejects values
     * like {@code not-an-email} that lack an {@code @} and a dotted domain.
     */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$");

    /**
     * Disposable email domains that are not accepted for an owner. Compared case-insensitively
     * against the normalized (lower-cased) address domain.
     */
    private static final Set<String> DISPOSABLE_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    private OwnerEmail() {
    }

    /** Whether {@code email} is a syntactically valid address. */
    static boolean isValid(String email) {
        return email != null && EMAIL.matcher(email).matches();
    }

    /**
     * Whether {@code email}'s domain is on the disposable-domain blocklist. Expects an address
     * already shown to be syntactically valid (so it contains a single {@code @}); the domain is
     * the part after the {@code @}, matched case-insensitively.
     */
    static boolean isDisposableDomain(String email) {
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        return DISPOSABLE_DOMAINS.contains(email.substring(at + 1).toLowerCase());
    }

    /** Lower-case and trim the email, or {@code null} when absent/blank. */
    static String normalize(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase();
    }
}

package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The single rule set for owner email addresses: what counts as "present", what counts as
 * a syntactically valid address, which domains are disposable, and how one is normalized for
 * storage. Shared so every step that touches an owner email treats it the same way.
 */
final class OwnerEmails {

    /** local-part {@code @} domain, no whitespace, and a dotted domain. */
    private static final Pattern SYNTAX = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** Domains of disposable/throwaway email providers that owners may not use. */
    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    private OwnerEmails() {
    }

    /** An email is present when it is non-null and not blank; blank is treated as omitted. */
    static boolean isPresent(String email) {
        return email != null && !email.isBlank();
    }

    /** Whether {@code email} is a syntactically valid address. */
    static boolean isValid(String email) {
        return email != null && SYNTAX.matcher(email).matches();
    }

    /** The stored form of an email: trimmed and lower-cased. */
    static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /** The domain part (after the {@code @}) of {@code email}, lower-cased, or {@code null}
     *  when the address has no domain. */
    static String domainOf(String email) {
        if (email == null) {
            return null;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return null;
        }
        return email.substring(at + 1).toLowerCase(Locale.ROOT);
    }

    /** Whether {@code email}'s domain belongs to a known disposable-email provider. */
    static boolean isDisposable(String email) {
        return DISPOSABLE_DOMAINS.contains(domainOf(email));
    }
}

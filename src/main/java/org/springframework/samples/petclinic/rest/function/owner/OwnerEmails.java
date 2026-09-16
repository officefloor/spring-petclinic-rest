package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The single rule set for owner email addresses: what counts as "present", what counts as
 * a syntactically valid address, which domains are disposable (or disposable-adjacent), and
 * how one is normalized for storage. Shared so every step that touches an owner email treats
 * it the same way.
 */
public final class OwnerEmails {

    /** local-part {@code @} domain, no whitespace, and a dotted domain. */
    private static final Pattern SYNTAX = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** Domains of disposable/throwaway email providers that owners may not use. */
    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    /** The disposable providers' second-level labels — the label immediately before the final
     *  dot of each disposable domain (e.g. "mailinator" from "mailinator.com"). Derived from
     *  {@link #DISPOSABLE_DOMAINS} so the two stay in sync. */
    private static final Set<String> DISPOSABLE_LABELS = DISPOSABLE_DOMAINS.stream()
            .map(OwnerEmails::secondLevelLabel)
            .collect(Collectors.toUnmodifiableSet());

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

    /** Whether {@code email}'s domain is disposable-adjacent: it shares a known disposable
     *  provider's second-level label (e.g. "mailinator" in "mailinator.net" or
     *  "sub.mailinator.com") without being an outright {@link #isDisposable disposable} domain.
     *  Such an address is accepted at create time but is a soft risk signal. */
    public static boolean isDisposableAdjacent(String email) {
        String domain = domainOf(email);
        if (domain == null || DISPOSABLE_DOMAINS.contains(domain)) {
            return false; // absent, or exactly disposable and so not merely adjacent
        }
        return DISPOSABLE_LABELS.contains(secondLevelLabel(domain));
    }

    /** The label immediately left of a domain's top-level label (e.g. "mailinator" from both
     *  "mailinator.com" and "sub.mailinator.com"), or {@code null} when {@code domain} has no
     *  dot to the left of a top-level label. */
    private static String secondLevelLabel(String domain) {
        int lastDot = domain.lastIndexOf('.');
        if (lastDot <= 0) {
            return null;
        }
        String withoutTld = domain.substring(0, lastDot);
        return withoutTld.substring(withoutTld.lastIndexOf('.') + 1);
    }
}

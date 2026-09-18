package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Email normalization shared by the owner pipelines: decides whether an address is
 * syntactically valid and produces its canonical (lower-cased) form, so an owner's
 * email is validated and stored in one consistent representation.
 */
public final class OwnerEmail {

    /** A syntactically valid address: {@code local@domain.tld} with no whitespace. */
    private static final Pattern VALID = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /** Domains of throwaway/disposable mailboxes that owners may not register with. */
    private static final Set<String> DISPOSABLE_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /** The distinctive service labels of the disposable domains ({@code mailinator},
     *  {@code tempmail}, {@code guerrillamail}) — the second-level name with the suffix
     *  stripped — used to recognise disposable-adjacent domains. */
    private static final Set<String> DISPOSABLE_LABELS = DISPOSABLE_DOMAINS.stream()
            .map(domain -> domain.substring(0, domain.indexOf('.')))
            .collect(Collectors.toUnmodifiableSet());

    private OwnerEmail() {
    }

    /** Whether {@code email} is a syntactically valid address. */
    public static boolean isValid(String email) {
        return email != null && VALID.matcher(email).matches();
    }

    /** The canonical, lower-cased form of {@code email}. */
    public static String normalize(String email) {
        return email.toLowerCase(Locale.ROOT);
    }

    /** Whether {@code email}'s domain is on the disposable-domain blocklist. */
    public static boolean isDisposable(String email) {
        String domain = domainOf(email);
        return domain != null && DISPOSABLE_DOMAINS.contains(domain);
    }

    /**
     * Whether {@code email}'s domain is <em>disposable-adjacent</em>: it carries a known
     * throwaway-mail service's label ({@code mailinator}, {@code tempmail},
     * {@code guerrillamail}) as one of its dot-separated labels. This catches close
     * relatives of a blocklisted domain that slip past {@link #isDisposable} — a subdomain
     * of one ({@code x.mailinator.com}) or the same service under a different suffix
     * ({@code mailinator.net}) — so an owner whose exact-match disposable address would have
     * been refused at creation can still be flagged as risky.
     */
    public static boolean isDisposableAdjacent(String email) {
        String domain = domainOf(email);
        if (domain == null) {
            return false;
        }
        for (String label : domain.split("\\.")) {
            if (DISPOSABLE_LABELS.contains(label)) {
                return true;
            }
        }
        return false;
    }

    /** The lower-cased domain part of {@code email}, or null when it carries no {@code '@'}. */
    private static String domainOf(String email) {
        if (email == null) {
            return null;
        }
        int at = email.lastIndexOf('@');
        return at < 0 ? null : email.substring(at + 1).toLowerCase(Locale.ROOT);
    }
}

package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Shared email handling for the owner pipelines: normalizes an address to its canonical
 * lower-cased form so validation, storage and the response all use the same value.
 */
final class OwnerEmails {

    /** A single {@code @} separating a non-empty local part from a dotted domain. */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    /** Throwaway-mailbox providers whose addresses an owner may not register with. */
    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Registrable labels of the {@link #DISPOSABLE_DOMAINS} (the {@code mailinator} of
     * {@code mailinator.com}). A domain is disposable-adjacent when it carries one of these,
     * catching subdomains and alternate TLDs the exact blocklist would miss.
     */
    private static final Set<String> DISPOSABLE_LABELS = DISPOSABLE_DOMAINS.stream()
            .map(OwnerEmails::registrableLabel)
            .collect(Collectors.toUnmodifiableSet());

    private OwnerEmails() {
    }

    /** Trimmed, lower-cased form of {@code email}; {@code null} stays {@code null}. */
    static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /** Whether {@code email} is a syntactically valid address. */
    static boolean isValid(String email) {
        return email != null && EMAIL.matcher(email).matches();
    }

    /** Whether {@code email}'s domain is on the disposable-domain blocklist. */
    static boolean isDisposableDomain(String email) {
        String domain = domainOf(email);
        return domain != null && DISPOSABLE_DOMAINS.contains(domain);
    }

    /**
     * Whether {@code email}'s domain is disposable-adjacent: not necessarily on the exact
     * blocklist (see {@link #isDisposableDomain}) but sharing a disposable provider's registrable
     * label, so subdomains ({@code inbox.mailinator.com}) and alternate TLDs
     * ({@code mailinator.net}) count while unrelated lookalikes ({@code mymailinator.com}) do not.
     */
    static boolean isDisposableAdjacentDomain(String email) {
        String domain = domainOf(email);
        return domain != null && DISPOSABLE_LABELS.contains(registrableLabel(domain));
    }

    /** The lower-cased domain part of {@code email}, or {@code null} when it has no {@code @}. */
    private static String domainOf(String email) {
        if (email == null) {
            return null;
        }
        int at = email.lastIndexOf('@');
        return at < 0 ? null : email.substring(at + 1).toLowerCase(Locale.ROOT);
    }

    /** The registrable label of {@code domain} - the label directly left of the TLD. */
    private static String registrableLabel(String domain) {
        String[] labels = domain.split("\\.");
        return labels.length >= 2 ? labels[labels.length - 2] : domain;
    }
}

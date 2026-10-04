package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Single source of truth for the owner email rule used by the {@code POST /owners} pipeline:
 * email is optional, but when supplied it must be a syntactically valid address, must not use a
 * disposable-email domain, and is stored and returned lower-cased. Centralised here (rather than
 * inlined in {@link NormalizeOwnerEmail}) so the notions of "present", "valid", "disposable" and
 * "normalised" stay defined in one place.
 */
public final class OwnerEmail {

    /**
     * A pragmatic syntactic check: a non-empty local part, a single {@code @}, and a domain with
     * at least one dot and non-empty labels, none containing whitespace or {@code @}.
     */
    private static final Pattern VALID = Pattern.compile("^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$");

    /** Disposable-email domains that are not accepted for owner registration. */
    private static final Set<String> DISPOSABLE_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Registrable base labels of the {@link #DISPOSABLE_DOMAINS} (the label immediately left of the
     * final TLD label, e.g. {@code mailinator} from {@code mailinator.com}). A stored email whose
     * domain shares one of these base labels — a different TLD or a subdomain of a disposable
     * domain — is "disposable-adjacent".
     */
    private static final Set<String> DISPOSABLE_BASE_LABELS = DISPOSABLE_DOMAINS.stream()
            .map(OwnerEmail::baseLabel)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());

    private OwnerEmail() {
    }

    /** An email is "present" when it is non-null and not blank. */
    static boolean isPresent(String email) {
        return email != null && !email.isBlank();
    }

    /** Whether the (present) value is a syntactically valid address. */
    static boolean isValid(String email) {
        return email != null && VALID.matcher(email.trim()).matches();
    }

    /** Whether the (valid) value's domain is on the disposable-email blocklist. */
    static boolean isDisposable(String email) {
        String domain = domainOf(email);
        return domain != null && DISPOSABLE_DOMAINS.contains(domain);
    }

    /**
     * Whether the (stored) email's domain is "disposable-adjacent": it is not itself on the
     * disposable blocklist (those are rejected at create time and never stored), but it shares its
     * registrable base label with a known disposable domain — a different TLD (e.g.
     * {@code mailinator.net}) or a subdomain (e.g. {@code mx.mailinator.com}). A null, blank or
     * syntactically invalid value is never disposable-adjacent.
     */
    public static boolean isDisposableAdjacent(String email) {
        if (!isPresent(email) || !isValid(email)) {
            return false;
        }
        String domain = domainOf(email);
        return domain != null && !DISPOSABLE_DOMAINS.contains(domain)
            && DISPOSABLE_BASE_LABELS.contains(baseLabel(domain));
    }

    /** The domain of a (normalized) email — the part after the last {@code @} — or {@code null}
     * when there is no {@code @}. The single definition of "the domain of an email". */
    private static String domainOf(String email) {
        String normalized = normalize(email);
        int at = normalized.lastIndexOf('@');
        return at < 0 ? null : normalized.substring(at + 1);
    }

    /**
     * The registrable base label of a domain: the label immediately left of the final TLD label
     * ({@code mailinator} for both {@code mailinator.com} and {@code mx.mailinator.net}), or the
     * whole string when it has no dot.
     */
    private static String baseLabel(String domain) {
        String[] labels = domain.split("\\.");
        if (labels.length < 2) {
            return domain;
        }
        return labels[labels.length - 2];
    }

    /** The stored/returned form: trimmed and lower-cased. */
    static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

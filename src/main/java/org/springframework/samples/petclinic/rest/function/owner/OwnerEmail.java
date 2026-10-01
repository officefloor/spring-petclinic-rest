package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The owner-email rule for the create pipeline's {@link ValidateOwnerFields}: email is optional, but
 * when supplied it must be a syntactically valid address whose domain is not a known disposable
 * provider, and it is lower-cased before being stored so it is returned lower-cased.
 *
 * <p>Mirrors {@link OwnerTelephone}. As with telephone, only the create path validates and
 * normalizes programmatically; the update path ({@link ValidateOwner}) leans on the
 * bean-validation {@code @Email} / {@code @Size} constraints on the generated DTO instead, so this
 * helper is intentionally not referenced there.
 */
final class OwnerEmail {

    /** Syntactic check: a non-empty local part, an {@code @}, and a dotted domain, no whitespace. */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    /** Disposable email providers whose addresses are rejected. */
    private static final Set<String> DISPOSABLE_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Registrable (second-level) names of the known disposable providers — e.g. "mailinator" for
     * "mailinator.com". Used to spot domains that are <em>adjacent</em> to a disposable provider
     * (same family) even though they are not an exact {@link #DISPOSABLE_DOMAINS} entry.
     */
    private static final Set<String> DISPOSABLE_BASES = DISPOSABLE_DOMAINS.stream()
            .map(OwnerEmail::baseLabel)
            .collect(Collectors.toUnmodifiableSet());

    private OwnerEmail() {
    }

    static boolean isValid(String value) {
        return value != null && EMAIL.matcher(value).matches();
    }

    /**
     * Whether the address's domain is a known disposable provider (see {@link #DISPOSABLE_DOMAINS}).
     * Only meaningful for a syntactically {@link #isValid valid} address.
     */
    static boolean isDisposable(String value) {
        if (value == null) {
            return false;
        }
        int at = value.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = value.substring(at + 1).toLowerCase(Locale.ROOT);
        return DISPOSABLE_DOMAINS.contains(domain);
    }

    /**
     * Whether the address's domain is <em>disposable-adjacent</em>: it shares its registrable
     * (second-level) name with a known {@link #DISPOSABLE_DOMAINS disposable} provider — for
     * instance a subdomain of one ({@code x.mailinator.com}) or the same name under a different TLD
     * ({@code mailinator.net}) — so it is in the same throwaway-email family without being an
     * exactly blocked disposable domain (those are rejected outright at creation). A blank or
     * malformed address, or a plain mainstream domain, is not adjacent.
     */
    static boolean isDisposableAdjacent(String value) {
        if (value == null) {
            return false;
        }
        int at = value.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = value.substring(at + 1).toLowerCase(Locale.ROOT);
        if (domain.isEmpty()) {
            return false;
        }
        return DISPOSABLE_BASES.contains(baseLabel(domain));
    }

    static String normalize(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    /**
     * The registrable (second-level) label of a dotted domain — the label immediately before the
     * public suffix, e.g. "mailinator" for "mailinator.com" and for "mail.mailinator.com". A domain
     * with no dot yields itself.
     */
    private static String baseLabel(String domain) {
        String[] labels = domain.split("\\.");
        return labels.length >= 2 ? labels[labels.length - 2] : domain;
    }
}

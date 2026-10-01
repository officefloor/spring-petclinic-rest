package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

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

    static String normalize(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }
}

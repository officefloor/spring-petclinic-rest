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
final class OwnerEmail {

    /**
     * A pragmatic syntactic check: a non-empty local part, a single {@code @}, and a domain with
     * at least one dot and non-empty labels, none containing whitespace or {@code @}.
     */
    private static final Pattern VALID = Pattern.compile("^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$");

    /** Disposable-email domains that are not accepted for owner registration. */
    private static final Set<String> DISPOSABLE_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

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
        String normalized = normalize(email);
        int at = normalized.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        return DISPOSABLE_DOMAINS.contains(normalized.substring(at + 1));
    }

    /** The stored/returned form: trimmed and lower-cased. */
    static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

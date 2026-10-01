package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * The owner-email rule for the create pipeline's {@link ValidateOwnerFields}: email is optional, but
 * when supplied it must be a syntactically valid address, and it is lower-cased before being stored
 * so it is returned lower-cased.
 *
 * <p>Mirrors {@link OwnerTelephone}. As with telephone, only the create path validates and
 * normalizes programmatically; the update path ({@link ValidateOwner}) leans on the
 * bean-validation {@code @Email} / {@code @Size} constraints on the generated DTO instead, so this
 * helper is intentionally not referenced there.
 */
final class OwnerEmail {

    /** Syntactic check: a non-empty local part, an {@code @}, and a dotted domain, no whitespace. */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private OwnerEmail() {
    }

    static boolean isValid(String value) {
        return value != null && EMAIL.matcher(value).matches();
    }

    static String normalize(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }
}

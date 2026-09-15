package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Shared email handling for the owner pipelines: decide whether an address is
 * syntactically valid and normalize it to a canonical lower-cased form. Used by
 * {@link NormalizeOwnerEmail} so create and update agree on what a stored email
 * looks like.
 */
final class EmailNormalizer {

    /**
     * A single, unquoted local part, an {@code @}, then a dotted domain with a
     * multi-character final label. Deliberately conservative: it rejects an address
     * with no {@code @} or no domain while accepting ordinary mailbox addresses.
     */
    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    private EmailNormalizer() {
    }

    static boolean isValid(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

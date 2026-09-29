package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

/**
 * The single definition of a valid, normalized owner email. An email is optional: a {@code null} or
 * blank value is left untouched (the owner simply has no email). When a non-blank value is present it
 * must be a syntactically valid address; the normalized form is the same address lower-cased.
 *
 * <p>Kept out of the function classes themselves because OfficeFloor requires each function class to
 * expose exactly one public method.
 */
final class EmailNormalizer {

    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$");

    /** A blank (null/empty/whitespace) value counts as "no email supplied". */
    static boolean isBlank(String email) {
        return email == null || email.isBlank();
    }

    static boolean isValid(String email) {
        return EMAIL.matcher(email.trim()).matches();
    }

    /** Lower-case the (already validated, non-blank) address. */
    static String normalize(String email) {
        return email.trim().toLowerCase();
    }

    private EmailNormalizer() {
    }
}

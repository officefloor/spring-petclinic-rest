package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

/**
 * Canonical email handling shared by the create pipeline: {@link NormalizeEmail} lower-cases the
 * stored form and validates syntax through it, and {@link IdentityKey} uses it for the email part of
 * the duplicate key. Not a pipeline step, so it is free to expose plain helpers.
 */
public final class EmailNormalizer {

    /** A syntactically valid address: a local part, an {@code @}, then a dotted domain. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private EmailNormalizer() {
    }

    /** Whether {@code email} is a syntactically valid address. */
    public static boolean isValid(String email) {
        return email != null && EMAIL.matcher(email).matches();
    }

    /** The stored form: trimmed and lower-cased. */
    public static String normalize(String email) {
        return email.trim().toLowerCase();
    }
}

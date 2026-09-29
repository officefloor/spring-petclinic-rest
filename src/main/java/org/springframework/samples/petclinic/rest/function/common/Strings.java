package org.springframework.samples.petclinic.rest.function.common;

/**
 * Shared null-safe String comparisons. Several owner functions compare names
 * case-insensitively while tolerating nulls; this is the single definition so the
 * pattern is not copy-pasted per function.
 */
public final class Strings {

    private Strings() {
    }

    /** Null-safe {@link String#equalsIgnoreCase}: two nulls are equal, a null and a value are not. */
    public static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}

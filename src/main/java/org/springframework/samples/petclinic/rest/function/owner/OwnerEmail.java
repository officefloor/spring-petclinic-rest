package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Email normalization shared by the owner pipelines: decides whether an address is
 * syntactically valid and produces its canonical (lower-cased) form, so an owner's
 * email is validated and stored in one consistent representation.
 */
public final class OwnerEmail {

    /** A syntactically valid address: {@code local@domain.tld} with no whitespace. */
    private static final Pattern VALID = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private OwnerEmail() {
    }

    /** Whether {@code email} is a syntactically valid address. */
    public static boolean isValid(String email) {
        return email != null && VALID.matcher(email).matches();
    }

    /** The canonical, lower-cased form of {@code email}. */
    public static String normalize(String email) {
        return email.toLowerCase(Locale.ROOT);
    }
}

package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Shared email handling: a syntactic validity check and canonicalization to lower case, so
 * equivalent addresses written with different letter cases are stored and returned in one
 * uniform form. Used to normalize an owner request's optional email before it is persisted.
 */
final class Emails {

    private static final Pattern VALID = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
                    + "@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?"
                    + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$");

    /** Throw-away email providers whose addresses must not be accepted. */
    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    private Emails() {
    }

    /** Whether {@code email} is a syntactically valid address. */
    static boolean isValid(String email) {
        return email != null && VALID.matcher(email).matches();
    }

    /** Returns {@code email} lower-cased: its canonical stored form. */
    static String normalize(String email) {
        return email.toLowerCase(Locale.ROOT);
    }

    /** The domain part of {@code email} (after the last {@code @}), lower-cased. */
    static String domainOf(String email) {
        return email.substring(email.lastIndexOf('@') + 1).toLowerCase(Locale.ROOT);
    }

    /** Whether {@code email}'s domain is a known disposable-email provider. */
    static boolean isDisposableDomain(String email) {
        return DISPOSABLE_DOMAINS.contains(domainOf(email));
    }
}

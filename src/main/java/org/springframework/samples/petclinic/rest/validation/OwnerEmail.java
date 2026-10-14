package org.springframework.samples.petclinic.rest.validation;

import java.util.Locale;
import java.util.Set;

/**
 * Validates optional owner email addresses against a blocklist of disposable email domains.
 */
public final class OwnerEmail {

    /** Disposable email domains; addresses at these domains or any of their subdomains are rejected. */
    public static final Set<String> DISPOSABLE_DOMAINS = Set.of(
        "mailinator.com",
        "tempmail.com",
        "guerrillamail.com");

    private OwnerEmail() {
    }

    /**
     * Checks an email address. An absent address is accepted; a present one must not belong to a
     * domain in {@link #DISPOSABLE_DOMAINS} (or a subdomain of one), compared case-insensitively.
     *
     * @param email the email address as supplied in the request, may be {@code null}
     * @throws InvalidEmailException if the address is at a disposable domain
     */
    public static void validate(String email) {
        if (email == null) {
            return;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return;
        }
        String domain = email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        if (domain.endsWith(".")) {
            domain = domain.substring(0, domain.length() - 1);
        }
        for (String blocked : DISPOSABLE_DOMAINS) {
            if (domain.equals(blocked) || domain.endsWith("." + blocked)) {
                throw new InvalidEmailException(email, "Email domain '" + domain + "' is a disposable email domain");
            }
        }
    }
}

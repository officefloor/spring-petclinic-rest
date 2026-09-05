package org.springframework.samples.petclinic.service;

import java.util.Locale;
import java.util.Set;

/**
 * Rejects an owner's email whose domain is a known disposable-mail provider. A
 * {@code null} or domain-less email is left untouched; a blocklisted domain raises
 * {@link IllegalArgumentException}, which the REST layer reports as 400 Bad Request.
 */
public final class DisposableEmailDomain {

    private static final Set<String> BLOCKED =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableEmailDomain() {
    }

    public static String validate(String email) {
        if (email != null) {
            int at = email.lastIndexOf('@');
            if (at >= 0 && BLOCKED.contains(email.substring(at + 1).toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Email domain is not allowed: " + email);
            }
        }
        return email;
    }
}

package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Single source of truth for what a "normalized" owner email is: lower-cased, so the
 * stored value and the email part of the identity key (see {@link EnsureUniqueHousehold})
 * are canonical regardless of the casing the client sent.
 * Syntactic validity is enforced upstream by the {@code @Email} constraint on the
 * request DTO; this step only canonicalizes an already-valid (or absent) address.
 */
final class OwnerEmails {

    private OwnerEmails() {
    }

    static String normalize(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }
}

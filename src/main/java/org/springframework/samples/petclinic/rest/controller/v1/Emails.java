package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Email-uniqueness rule for owners: an owner's email must be unique across all
 * owners when compared case-insensitively. Owners without an email never clash.
 */
final class Emails {

    private Emails() {
    }

    /** Whether {@code candidate}'s email is already used by an existing owner. */
    static boolean isDuplicate(Collection<Owner> existing, Owner candidate) {
        String email = normalize(candidate.getEmail());
        if (email == null) {
            return false;
        }
        return existing.stream().anyMatch(other -> email.equals(normalize(other.getEmail())));
    }

    private static String normalize(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.strip().toLowerCase(Locale.ROOT);
    }
}

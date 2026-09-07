package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's preferred contact channel: "EMAIL" when an email is present,
 * otherwise "PHONE". A null or blank email counts as no email.
 */
public final class ContactPreference {

    private ContactPreference() {
    }

    /** "EMAIL" when {@code owner} has a non-blank email, otherwise "PHONE". */
    public static String of(Owner owner) {
        String email = owner.getEmail();
        return email != null && !email.isBlank() ? "EMAIL" : "PHONE";
    }
}

package org.springframework.samples.petclinic.rest.function.common;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the owner's preferred contact channel for the read-only {@code contactPreference}
 * field: {@code EMAIL} when the owner has an email address on record, otherwise {@code PHONE}.
 */
public final class ContactPreferences {

    private static final String EMAIL = "EMAIL";

    private static final String PHONE = "PHONE";

    private ContactPreferences() {
    }

    /** {@code EMAIL} when {@code owner} has a non-blank email address, otherwise {@code PHONE}. */
    public static String of(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank() ? EMAIL : PHONE;
    }
}

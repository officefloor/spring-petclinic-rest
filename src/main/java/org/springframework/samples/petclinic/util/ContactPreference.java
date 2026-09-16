package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's preferred contact channel, computed on read. An owner with an email
 * address prefers {@link #EMAIL}; otherwise the fallback is {@link #PHONE}.
 */
public final class ContactPreference {

    /** Preference for owners that have an email address. */
    public static final String EMAIL = "EMAIL";

    /** Preference for owners without an email address. */
    public static final String PHONE = "PHONE";

    private ContactPreference() {
    }

    /** The contact preference for the given owner: {@link #EMAIL} when an email address
     *  is present, otherwise {@link #PHONE}. */
    public static String of(Owner owner) {
        return (owner.getEmail() != null && !owner.getEmail().isBlank()) ? EMAIL : PHONE;
    }
}

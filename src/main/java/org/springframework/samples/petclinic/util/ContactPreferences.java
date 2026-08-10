package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code contactPreference}: {@code "EMAIL"} when the owner has a
 * non-blank email, otherwise {@code "PHONE"}.
 */
public final class ContactPreferences {

    /** Preference returned when the owner has a usable email address. */
    public static final String EMAIL = "EMAIL";

    /** Preference returned when the owner has no email and must be reached by phone. */
    public static final String PHONE = "PHONE";

    private ContactPreferences() {
    }

    /**
     * @param owner the pet owner.
     * @return {@link #EMAIL} when the owner's email is present (non-null, non-blank),
     *         otherwise {@link #PHONE}.
     */
    public static String preferenceFor(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank() ? EMAIL : PHONE;
    }
}

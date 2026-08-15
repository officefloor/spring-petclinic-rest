package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code contactPreference} from its stored fields: {@code "EMAIL"}
 * when an email is present, otherwise {@code "PHONE"}.
 */
public final class ContactPreference {

    /** Preferred channel when the owner has an email address. */
    public static final String EMAIL = "EMAIL";

    /** Preferred channel when the owner has no email address. */
    public static final String PHONE = "PHONE";

    private ContactPreference() {
    }

    /** Returns the contact preference for {@code owner} from its stored fields. */
    public static String preferenceOf(Owner owner) {
        String email = owner.getEmail();
        return (email != null && !email.isEmpty()) ? EMAIL : PHONE;
    }
}

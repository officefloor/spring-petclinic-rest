package org.springframework.samples.petclinic.model;

/**
 * Derives an owner's preferred contact channel from their own fields:
 * 'EMAIL' when an email is present, otherwise 'PHONE'.
 */
public final class ContactPreference {

    /** Returned when the owner has an email address. */
    public static final String EMAIL = "EMAIL";

    /** Returned when the owner has no email address. */
    public static final String PHONE = "PHONE";

    private ContactPreference() {
    }

    /** 'EMAIL' when the owner has a non-empty email, otherwise 'PHONE'. */
    public static String of(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isEmpty() ? EMAIL : PHONE;
    }
}

package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Preferred contact channel for a pet owner: {@code EMAIL} when the owner has an email address
 * (non-null, non-blank), otherwise {@code PHONE}. Derived purely from the owner's own stored
 * state, so it carries no stored data and is seed-independent. Used by the owner mapper to expose
 * {@code contactPreference} on responses.
 */
public final class ContactPreference {

    private ContactPreference() {
    }

    /**
     * {@code EMAIL} when the email is present (non-null, non-blank), otherwise {@code PHONE}.
     */
    public static String of(String email) {
        boolean hasEmail = email != null && !email.isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }
}

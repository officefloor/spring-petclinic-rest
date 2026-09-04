package org.springframework.samples.petclinic.mapper;

/**
 * Derives the owner's preferred contact channel: {@code EMAIL} when an email is on
 * file, otherwise {@code PHONE}. The value is a pure function of the email, so it needs
 * no stored state.
 */
public final class ContactPreference {

    private ContactPreference() {
    }

    public static String of(String email) {
        return email != null && !email.isBlank() ? "EMAIL" : "PHONE";
    }
}

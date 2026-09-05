package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Derives the {@code contactPreference}: {@code EMAIL} when an email address is
 * present, otherwise {@code PHONE}.
 */
public final class ContactPreference {

    private ContactPreference() {
    }

    public static String of(String email) {
        return (email != null && !email.isBlank()) ? "EMAIL" : "PHONE";
    }
}

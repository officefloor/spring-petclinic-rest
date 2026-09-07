package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Salutation for a pet owner: the honorific {@code title} (e.g. MR/MRS/MS/DR) followed by a single
 * space and the owner's last name, or just the last name when no title was supplied. Derived purely
 * from the owner's own stored state, so it carries no stored data and is seed-independent. Used by
 * the owner mapper to expose {@code salutation} on responses.
 */
public final class Salutation {

    private Salutation() {
    }

    /**
     * {@code title + " " + lastName} when a title is present (non-null, non-blank), otherwise just
     * {@code lastName}.
     */
    public static String of(String title, String lastName) {
        boolean hasTitle = title != null && !title.isBlank();
        return hasTitle ? title + " " + lastName : lastName;
    }
}

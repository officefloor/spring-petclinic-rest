package org.springframework.samples.petclinic.model;

/**
 * Composes an owner's salutation from an optional title (MR/MRS/MS/DR) and last name:
 * {@code title + " " + lastName}, or just the last name when no title is given.
 */
public final class Salutations {

    private Salutations() {
    }

    /** The salutation for {@code title} and {@code lastName}. */
    public static String compose(String title, String lastName) {
        return (title == null || title.isBlank()) ? lastName : title + " " + lastName;
    }
}

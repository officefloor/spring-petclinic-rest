package org.springframework.samples.petclinic.model;

/**
 * Derives an owner's salutation from their own fields: the title followed by a single
 * space and the lastName when a title is present (e.g. 'DR who'), otherwise just the
 * lastName.
 */
public final class Salutation {

    private Salutation() {
    }

    /** '{title} {lastName}' when the owner has a non-empty title, otherwise just the lastName. */
    public static String of(Owner owner) {
        String title = owner.getTitle();
        String lastName = owner.getLastName();
        if (title == null || title.isEmpty()) {
            return lastName;
        }
        return title + " " + lastName;
    }
}

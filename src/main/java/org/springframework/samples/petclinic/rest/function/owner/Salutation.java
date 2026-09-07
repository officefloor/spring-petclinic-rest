package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's 'salutation' from their (optional) title and last name: the
 * title and last name joined by a single space (e.g. "DR Franklin") when a title
 * was supplied, or just the last name when it was absent.
 */
public final class Salutation {

    private Salutation() {
    }

    /** The owner's salutation: {@code '<title> <lastName>'}, or just the last name when no title. */
    public static String of(Owner owner) {
        String title = owner.getTitle();
        String lastName = owner.getLastName();
        if (title == null || title.isBlank()) {
            return lastName;
        }
        return title + " " + lastName;
    }
}

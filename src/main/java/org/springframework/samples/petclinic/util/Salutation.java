package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Composes an owner's salutation, computed on read. When the owner has an honorific
 * title, the salutation is the title and last name separated by a single space (e.g.
 * "DR Franklin"); otherwise it is just the last name.
 */
public final class Salutation {

    private Salutation() {
    }

    /** The salutation for the given owner: {@code title + " " + lastName} when a title
     *  is present, otherwise the last name alone. */
    public static String of(Owner owner) {
        String title = owner.getTitle();
        return (title != null && !title.isBlank()) ? title + " " + owner.getLastName() : owner.getLastName();
    }
}

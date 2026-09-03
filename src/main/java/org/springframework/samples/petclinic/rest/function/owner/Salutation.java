package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's salutation: the title and last name (e.g. "DR Franklin"),
 * or just the last name when no title is given.
 */
public class Salutation {

    public static String of(Owner owner) {
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }
}

package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Composes an owner's salutation: the title and last name (e.g. {@code "DR who"})
 * when a title is present, otherwise just the last name.
 */
public final class Salutation {

    private Salutation() {
    }

    public static String of(Owner owner) {
        String title = owner.getTitle();
        return title == null || title.isBlank() ? owner.getLastName() : title + " " + owner.getLastName();
    }
}

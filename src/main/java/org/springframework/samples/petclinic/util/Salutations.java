package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Composes an owner's salutation from the optional honorific {@code title} and the last name.
 * When a non-blank title is present the salutation is the title, a single space, then the last
 * name (e.g. {@code "DR who"}); when no title was supplied it is just the last name.
 */
public final class Salutations {

    private Salutations() {
    }

    /**
     * @param owner the owner (must not be {@code null}).
     * @return {@code title + " " + lastName} when the owner has a non-blank title, otherwise the
     *         last name alone.
     */
    public static String salutationFor(Owner owner) {
        String lastName = owner.getLastName();
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return lastName;
        }
        return title + " " + lastName;
    }
}

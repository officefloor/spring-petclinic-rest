package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the {@code salutation} field produced by {@link OwnerMapper#toOwnerDto}: the owner's
 * title followed by a single space and the lastName when a title was supplied, otherwise just the
 * lastName.
 */
public final class Salutation {

    private Salutation() {
    }

    /**
     * Derive the salutation for an owner.
     *
     * @param owner the owner; its title and lastName drive the result
     * @return {@code title + " " + lastName} when a non-blank title is present, else {@code lastName}
     */
    public static String of(Owner owner) {
        String title = owner.getTitle();
        if (title != null && !title.isBlank()) {
            return title + " " + owner.getLastName();
        }
        return owner.getLastName();
    }
}

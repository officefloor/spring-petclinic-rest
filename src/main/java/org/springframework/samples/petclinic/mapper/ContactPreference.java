package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code contactPreference}: 'EMAIL' when the owner has a non-blank
 * email, otherwise 'PHONE'. Kept out of {@link OwnerMapper} so MapStruct does not
 * mistake the helper for an implicit mapping method.
 */
public final class ContactPreference {

    private ContactPreference() {
    }

    /** The preferred contact channel for {@code owner}. */
    public static String of(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank() ? "EMAIL" : "PHONE";
    }
}

package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code selfLink}: the canonical API path to the owner, formed as
 * {@code /api/owners/} followed by the owner's id. Returns {@code null} when the owner has no id.
 * Kept out of {@link OwnerMapper} so MapStruct does not mistake the helper for an implicit
 * mapping method.
 */
public final class SelfLink {

    private SelfLink() {
    }

    /** The API path for {@code owner}, or {@code null} when it has no id. */
    public static String of(Owner owner) {
        return owner.getId() == null ? null : "/api/owners/" + owner.getId();
    }
}

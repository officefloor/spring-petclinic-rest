package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: every owner exposes a {@code selfLink} = {@code /api/owners/} followed by its
 * id, the canonical GET path for that owner. Kept as a small, self-contained unit so the link
 * can be derived in the mapper without adding complexity to the controller or DTO.
 */
public final class OwnerSelfLinkPolicy {

    private OwnerSelfLinkPolicy() {
    }

    /** The owner's canonical self link: {@code /api/owners/{id}}. */
    public static String selfLink(Owner owner) {
        return "/api/owners/" + owner.getId();
    }
}

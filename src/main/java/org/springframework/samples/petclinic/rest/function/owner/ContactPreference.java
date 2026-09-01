package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's preferred contact method: {@code EMAIL} when an email is
 * present, otherwise {@code PHONE}.
 */
public final class ContactPreference {

    private ContactPreference() {
    }

    public static String of(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank() ? "EMAIL" : "PHONE";
    }
}
